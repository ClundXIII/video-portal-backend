package co.clai.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.Platform;
import co.clai.video.db.model.User;
import co.clai.video.module.Api;
import co.clai.video.util.ResourceUtil;
import co.clai.video.util.log.LoggingUtil;
import junit.framework.TestCase;

public class ApiTest extends TestCase implements HttpTest {

	public ApiTest(String name) {
		super(name);
	}

	public void testViewApi() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();
		String baseUrl = getIpAndPort(jData);

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json")).getJSONObject("credentials");

		Platform.populateTestPlatforms(l.getDbCon(), credentialData);

		User.addNewLocalUser(l.getDbCon(), "correctUser", "correctPassword", "email@test.com", true);

		startHttpListener(l);

		logger.log(Level.INFO, "testing viewing api");

		assertTrue(httpRequestAsUser("correctUser", "correctPassword", baseUrl, baseUrl + "/" + Api.LOCATION)
				.contains("API Documentation:"));
	}
}