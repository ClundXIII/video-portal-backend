package co.clund.oauth2;

import java.util.HashMap;
import java.util.Map;
import co.clund.util.TriFunction;

import co.clund.UserSession;

public class OAuth2CallbackHandler {

	public OAuth2CallbackHandler() {
	}

	Map<String, TriFunction<String, UserSession, Map<String, String[]>, String>> stateCallbackMap = new HashMap<>();

	public void insertCallback(String state, TriFunction<String, UserSession, Map<String, String[]>, String> callback) {
		stateCallbackMap.put(state, callback);
	}

	public String call(String state, String platKey, UserSession s, Map<String, String[]> parameters) {
		return stateCallbackMap.get(state).apply(platKey, s, parameters);
	}

}
