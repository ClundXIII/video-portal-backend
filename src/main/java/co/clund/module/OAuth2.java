
package co.clund.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;

import org.apache.http.client.utils.URIBuilder;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.DBOAuth2Platform;
import co.clund.db.model.UserOAuth2Connection;
import co.clund.html.HtmlPage;
import co.clund.html.HtmlTable;
import co.clund.html.HtmlTable.HtmlTableRow;
import co.clund.module.FunctionResult.Status;
import co.clund.oauth2.AbstractOAuth2UserPlatform;
import co.clund.oauth2.AbstractOAuth2UserPlatform.TokenData;
import co.clund.util.RandomUtil;
import co.clund.util.TriFunction;
import co.clund.util.cache.DynamicAsyncExpiringCache;

public class OAuth2 extends AbstractModule {

	private class StateInfo {
		public final String platformKey;
		public final String callbackFunc;

		public StateInfo(String platformKey, String callbackFunc) {
			this.platformKey = platformKey;
			this.callbackFunc = callbackFunc;
		}
	}

	private static final String GET_PARAM_STATE = "state";

	private static final String CALLBACK_FUNC_INITIAL_CONNECT = "initial_connect";

	public static final String FUNCTION_NAME_CALLBACK = "callback";

	public static final String LOCATION = "oauth2";

	private final DynamicAsyncExpiringCache<StateInfo> dynAsyncCache;

	public OAuth2(@SuppressWarnings("unused") AbstractModule parent, DatabaseConnector dbCon) {
		super("", LOCATION, dbCon);

		dynAsyncCache = new DynamicAsyncExpiringCache<>("oauth2TokenCache_" + dbCon.getListener().getSiteUrl(),
				60 * 10); // 10 Minutes lifetime

		dbCon.getListener().getCallBackHandler().insertCallback(CALLBACK_FUNC_INITIAL_CONNECT,
				this.initialConnectCallback);
	}

	TriFunction<String, UserSession, Map<String, String[]>, String> initialConnectCallback = (String platformKey,
			UserSession session, Map<String, String[]> parameters) -> {

		AbstractOAuth2UserPlatform abPlat = AbstractOAuth2UserPlatform
				.getAbstractOAuth2UserPlatformFromType(DBOAuth2Platform.getPlatformByKey(dbCon, platformKey));

		TokenData clientCredData = abPlat.getClientCredentialsFromCallback(dbCon, parameters);

		UserOAuth2Connection.addNewUserOAuth2Connection(dbCon, session.getThisUser().getId(),
				abPlat.getdBOAuth2Platform().getId(), "", clientCredData);

		return "/" + LOCATION;
	};

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		HtmlPage p = new HtmlPage("Connect to OAuth2 Platforms", s, dbCon, "/" + LOCATION);

		p.writeH1("Supported Platforms:");

		HtmlTable tab = new HtmlTable();

		tab.addHeader(Arrays.asList("Platform Name", "Status", "Add/Remove", "Extra States"));

		for (DBOAuth2Platform plat : DBOAuth2Platform.getAllPlatform(dbCon)) {
			HtmlTableRow row = tab.new HtmlTableRow();

			row.writeText(plat.getName());

			UserOAuth2Connection con = UserOAuth2Connection.getUserOAuth2ConnectionByUserPlatformId(dbCon,
					s.getThisUser().getId(), plat.getId());

			AbstractOAuth2UserPlatform abPlat = AbstractOAuth2UserPlatform.getAbstractOAuth2UserPlatformFromType(plat);

			if (con == null) {
				row.writeText("not connected");
				URIBuilder builder = abPlat.getClientCredentialsRequestBuilder(dbCon);

				String token = RandomUtil.getRandomString(150);
				StateInfo statInf = new StateInfo(plat.getKey(), CALLBACK_FUNC_INITIAL_CONNECT);
				dynAsyncCache.put(token, statInf);

				builder.addParameter("state", token);
				row.writeLink(builder.toString(), "connect");
				row.writeText("");
			} else {
				row.writeText("connected");
				// row.writeLink(abPlat.getClientCredentialsUnlinkBuilder(dbCon).toStrting(),
				// "remove");
				row.writeText("remove");
				StringBuilder sb = new StringBuilder();
				for (String str : con.getStates()) {
					sb.append(str + ",");
				}
				row.writeText(sb.toString().substring(0, sb.toString().length()));
			}

			tab.write(row);

		}

		p.write(tab);

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		allFunctions.put(FUNCTION_NAME_CALLBACK, this::callback);

		return allFunctions;
	}

	private FunctionResult callback(UserSession s, Map<String, String[]> parameters) {

		String token = parameters.get(GET_PARAM_STATE)[0];

		StateInfo data = dynAsyncCache.retrieve(token);

		if (data == null) {
			logger.log(Level.WARNING, "data is null @OAuth2.callback");
			return new FunctionResult("".getBytes());//Status.OK, LOCATION);
		}

		String retVal = dbCon.getListener().getCallBackHandler().call(data.callbackFunc, data.platformKey, s,
				parameters);

		return new FunctionResult(Status.OK, retVal);
	}

}
