
package co.clund.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.module.FunctionResult.Status;

public class OAuth2 extends AbstractModule {

	private static final String GET_PARAM_STATE = "state";

	public static final String FUNCTION_NAME_CALLBACK = "callback";

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

		allFunctions.put(FUNCTION_NAME_CALLBACK, this::callback);

		return allFunctions;
	}

	private FunctionResult callback(UserSession s, Map<String, String[]> parameters) {

		String retVal = dbCon.getListener().getCallBackHandler().call(parameters.get(GET_PARAM_STATE)[0], s,
				parameters);

		return new FunctionResult(Status.OK, retVal);
	}

}
