package co.clund.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;
import co.clund.html.HtmlForm;
import co.clund.html.HtmlPage;
import co.clund.submodule.video.VideoIndex;

public class Profile extends AbstractModule {

	public static final String FUNCTION_NAME_LOGOUT = "logout";
	public static final String FUNCTION_NAME_LOGIN = "login";

	public static final String LOCATION = "profile";

	public static final String LOGIN_FORM_NAME_USERNAME = "username";
	public static final String LOGIN_FORM_NAME_PASSWORD = "password";
	public static final String LOGIN_FORM_NAME_LOCATION = "location";

	public Profile(@SuppressWarnings("unused") AbstractModule parent, DatabaseConnector dbCon) {
		super("", LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage b;

		User thisUser = s.getThisUser();
		if (thisUser == null) {
			b = new HtmlPage("Login", s, dbCon, "/" + LOCATION);
			b.writeWithoutEscaping(HtmlPage.getMessage(parameters));

			b.writeH1("Log in with Username and Password");

			HtmlForm r = new HtmlForm(LOCATION + "." + FUNCTION_NAME_LOGIN, HtmlForm.Method.POST);

			r.addTextElement("Username", LOGIN_FORM_NAME_USERNAME, "");
			r.addPasswordElement("Password", LOGIN_FORM_NAME_PASSWORD);
			r.addSubmit("Login", HtmlForm.ButtonType.SUCCESS);

			b.write(r);

			return b.finish().getBytes();
		}

		b = new HtmlPage("Overview", s, dbCon, "/" + LOCATION);
		b.writeWithoutEscaping(HtmlPage.getMessage(parameters));
		b.writeText("Logged in as User: " + thisUser.getUsername());

		b.writeHline();

		b.writeLink(Settings.LOCATION, "change Settings");

		b.writeHline();

		return b.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		allFunctions.put(FUNCTION_NAME_LOGIN, this::login);
		allFunctions.put(FUNCTION_NAME_LOGOUT, this::logout);

		return allFunctions;
	}

	private FunctionResult login(UserSession s, Map<String, String[]> parameters) {

		if (s.getThisUser() != null) {
			return new FunctionResult(FunctionResult.Status.MALFORMED_REQUEST, getModuleName());
		}

		try {
			String username = parameters.get(LOGIN_FORM_NAME_USERNAME)[0];
			String password = parameters.get(LOGIN_FORM_NAME_PASSWORD)[0];

			User u = User.login(dbCon, username, password);

			if (u == null) {
				return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(), "failed login");
			}

			s.setUser(u);
		} catch (Exception e) {
			e.printStackTrace();
			return new FunctionResult(FunctionResult.Status.INTERNAL_ERROR, getModuleName());
		}

		return new FunctionResult(FunctionResult.Status.OK, VideoIndex.INDEX_LOCATION);
	}

	private FunctionResult logout(UserSession s, @SuppressWarnings("unused") Map<String, String[]> parameters) {
		if (s.getThisUser() == null) {
			return new FunctionResult(FunctionResult.Status.MALFORMED_REQUEST, getModuleName(), "not logged in");
		}

		s.setUser(null);
		s.clear();

		return new FunctionResult(FunctionResult.Status.OK, getModuleName());
	}

}
