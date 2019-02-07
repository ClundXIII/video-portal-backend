package co.clai.video.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.mindrot.jbcrypt.BCrypt;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.User;
import co.clai.video.html.HtmlForm;
import co.clai.video.html.HtmlPage;
import co.clai.video.UserSession;

public class Settings extends AbstractModule {

	private static final String FUNCTION_NAME_CHANGE_OWN_PASSWORD = "changeOwnPassword";
	public static final String LOCATION = "settings";
	private static String HTML_OLD_PASSWORD_ID = "old_password";
	private static String HTML_NEW_PASSWORD1_ID = "new_password1";
	private static String HTML_NEW_PASSWORD2_ID = "new_password2";

	public Settings(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		HtmlPage p = new HtmlPage("User settings", null, null, s);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		p.writeH1("Change Password");

		HtmlForm changePwdForm = new HtmlForm(LOCATION + "." + FUNCTION_NAME_CHANGE_OWN_PASSWORD, HtmlForm.Method.POST);
		changePwdForm.addPasswordElement("Old Password", HTML_OLD_PASSWORD_ID);
		changePwdForm.addPasswordElement("New Password", HTML_NEW_PASSWORD1_ID);
		changePwdForm.addPasswordElement("Repeat new Password", HTML_NEW_PASSWORD2_ID);
		changePwdForm.addSubmit("Change Password", HtmlForm.ButtonType.WARNING);

		p.write(changePwdForm);

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> retMap = new HashMap<>();

		retMap.put(FUNCTION_NAME_CHANGE_OWN_PASSWORD, this::changeOwnPassword);

		return retMap;
	}

	private FunctionResult changeOwnPassword(UserSession s, Map<String, String[]> parameters) {

		String old_password = parameters.get(HTML_OLD_PASSWORD_ID)[0];
		String new_password1 = parameters.get(HTML_NEW_PASSWORD1_ID)[0];
		String new_password2 = parameters.get(HTML_NEW_PASSWORD2_ID)[0];

		if (!new_password1.equals(new_password2)) {
			return new FunctionResult(FunctionResult.Status.MALFORMED_REQUEST, getModuleName(),
					"new passwords don't match");
		}

		User thisUser = s.getThisUser();
		if (BCrypt.checkpw(old_password, thisUser.getEncryptedPassword())) {

			thisUser.setNewPassword(dbCon, new_password1);

			return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(), "password changed successfully");
		}

		return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(), "old password is not correct");
	}
}
