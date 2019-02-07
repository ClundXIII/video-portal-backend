
package co.clai.video.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.AbstractDbTable;
import co.clai.video.db.model.Platform;
import co.clai.video.module.FunctionResult.Status;
import co.clai.video.platform.AbstractPlatform;
import co.clai.video.UserSession;

public class OAuth2 extends AbstractModule {

	private static final String SESSION_KEY_OAUTH2_PLATFORM = "oauth2Platform";

	public static final String FUNCTION_NAME_CALLBACK = "callback";
	public static final String FUNCTION_NAME_CONNECT = "connect";

	public static final String LOCATION = "oauth2";

	public OAuth2(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return "".getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		allFunctions.put(FUNCTION_NAME_CONNECT, this::connect);
		allFunctions.put(FUNCTION_NAME_CALLBACK, this::callback);

		return allFunctions;
	}

	private FunctionResult connect(UserSession s, Map<String, String[]> parameters) {

		int platformId = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(Platform.getPlatformById(dbCon, platformId));

		s.getSession().setAttribute(SESSION_KEY_OAUTH2_PLATFORM, new Integer(platformId));

		return new FunctionResult(Status.OK, abPlat.getOAuth2ConnectRedirect(dbCon), "Redirecting");
	}

	private FunctionResult callback(UserSession s, Map<String, String[]> parameters) {
		return null;
	}

}
