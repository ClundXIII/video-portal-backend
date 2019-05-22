package co.clund.oauth2;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;

public class OAuth2CallbackHandler {

	public OAuth2CallbackHandler() {
	}

	Map<String, BiFunction<UserSession, Map<String, String[]>, String>> stateCallbackMap = new HashMap<>();

	public void insertCallback(String state, BiFunction<UserSession, Map<String, String[]>, String> callback) {
		stateCallbackMap.put(state, callback);
	}

	public String call(String state, UserSession s, Map<String, String[]> parameters) {
		return stateCallbackMap.get(state).apply(s, parameters);
	}

}
