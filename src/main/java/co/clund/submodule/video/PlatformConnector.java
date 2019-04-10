package co.clund.submodule.video;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.module.AbstractModule;
import co.clund.module.FunctionResult;

public class PlatformConnector extends AbstractModule {

	public static final String LOCATION = "platforms";

	private static final String FUNCTION_NAME_ADD = "connectNew";
	private static final String FUNCTION_NAME_EDIT = "requestUpload";

	public PlatformConnector(AbstractModule parent, DatabaseConnector dbCon) {
		super(parent.getModulePath(), LOCATION, dbCon);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		// TODO Auto-generated method stub
		return new byte[1];
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		// TODO Auto-generated method stub
		return new HashMap<>();
	}

}
