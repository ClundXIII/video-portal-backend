
package co.clund.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.http.client.utils.URIBuilder;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.AbstractDbTable;
import co.clund.module.FunctionResult.Status;
import co.clund.submodule.video.dbmodel.Platform;
import co.clund.submodule.video.platform.AbstractPlatform;

public class OAuth2 extends AbstractModule {

	private static final String STATE_REQUEST_UPLOAD_ACCESS = "requestUploadAccess";
	private static final String STATE_REQUEST_LINK = "requestLink";

	private static final String SESSION_KEY_OAUTH2_PLATFORM = "oauth2Platform";

	public static final String FUNCTION_NAME_CALLBACK = "callback";

	public static final String FUNCTION_NAME_REQUEST_LINK = STATE_REQUEST_LINK;
	public static final String FUNCTION_NAME_REQUEST_UPLOAD = STATE_REQUEST_UPLOAD_ACCESS;

	public static final String LOCATION = "oauth2";

	public OAuth2(@SuppressWarnings("unused") AbstractModule parent, DatabaseConnector dbCon) {
		super("", LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return "you are not supposed to see this".getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		allFunctions.put(FUNCTION_NAME_REQUEST_LINK, this::requestLink);
		allFunctions.put(FUNCTION_NAME_REQUEST_UPLOAD, this::requestUploadAccess);
		allFunctions.put(FUNCTION_NAME_CALLBACK, this::callback);

		return allFunctions;
	}

	private FunctionResult requestLink(UserSession s, Map<String, String[]> parameters) {

		int platformId = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(Platform.getPlatformById(dbCon, platformId));

		s.getSession().setAttribute(SESSION_KEY_OAUTH2_PLATFORM, new Integer(platformId));

		URIBuilder builder = abPlat.getClientCredentialsRequestBuilder(dbCon);

		builder.addParameter("state", STATE_REQUEST_LINK);

		return new FunctionResult(Status.OK, builder.toString(), "Redirecting");
	}

	private FunctionResult requestUploadAccess(UserSession s, Map<String, String[]> parameters) {

		int platformId = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(Platform.getPlatformById(dbCon, platformId));

		s.getSession().setAttribute(SESSION_KEY_OAUTH2_PLATFORM, new Integer(platformId));

		URIBuilder builder = abPlat.getClientCredentialsUploadRequestBuilder(dbCon);

		builder.addParameter("state", STATE_REQUEST_UPLOAD_ACCESS);

		return new FunctionResult(Status.OK, builder.toString(), "Redirecting");
	}

	private FunctionResult callback(UserSession s, Map<String, String[]> parameters) {

		String state = parameters.get("state")[0];

		switch (state) {

		case STATE_REQUEST_LINK:
			break;

		case STATE_REQUEST_UPLOAD_ACCESS:
			break;

		default:
			// errors and such
			break;

		}

		return null;
	}

}
