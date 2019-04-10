package co.clund.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.Platform;
import co.clund.db.model.User;
import co.clund.module.FunctionResult;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
import junit.framework.TestCase;

public class ProfileTest extends TestCase implements HttpTest {

	public ProfileTest(String name) {
		super(name);
	}

	public void testLogin() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();
		String baseUrl = getIpAndPort(jData);

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json")).getJSONObject("credentials");

		Platform.populateTestPlatforms(l.getDbCon(), credentialData);

		User.addNewLocalUser(l.getDbCon(), "correctUser", "correctPassword", "email@test.com", false);

		startHttpListener(l);

		logger.log(Level.INFO, "testing login with no params");
		assertTrue(httpRequest(baseUrl + "/profile.login").trim().equals(FunctionResult.Status.INTERNAL_ERROR.name()));

		logger.log(Level.INFO, "testing unknown user credentials");
		assertTrue(httpRequest(baseUrl + "/profile.login?location=0&username=wrongUser&password=wrongPassword").trim()
				.equals(FunctionResult.Status.FAILED.name()));

		logger.log(Level.INFO, "testing wrong credentials");
		assertTrue(httpRequest(baseUrl + "/profile.login?location=0&username=correctUser&password=wrongPassword").trim()
				.equals(FunctionResult.Status.FAILED.name()));

		logger.log(Level.INFO, "testing correct credentials");
		assertTrue(httpRequest(baseUrl + "/profile.login?location=0&username=correctUser&password=correctPassword")
				.trim().equals(FunctionResult.Status.OK.name()));

		logger.log(Level.INFO, "changing password");

		User thisUser = User.getUserById(l.getDbCon(), 1);
		thisUser.setNewPassword(l.getDbCon(), "newPassword");

		logger.log(Level.INFO, "testing new changed credentials");
		assertTrue(httpRequest(baseUrl + "/profile.login?location=0&username=correctUser&password=newPassword").trim()
				.equals(FunctionResult.Status.OK.name()));

		l.stop_join();
	}

}
