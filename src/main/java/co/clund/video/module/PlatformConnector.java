package co.clund.video.module;

import java.util.Map;
import java.util.function.BiFunction;

import co.clund.video.UserSession;
import co.clund.video.db.DatabaseConnector;

public class PlatformConnector extends AbstractModule {

	public static final String LOCATION = "platforms";

	private static final String FUNCTION_NAME_ADD = "connectNew";
	private static final String FUNCTION_NAME_EDIT = "requestUpload";

	public PlatformConnector(String name, DatabaseConnector dbCon) {
		super(name, dbCon);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		// TODO Auto-generated method stub
		return null;
	}

}
