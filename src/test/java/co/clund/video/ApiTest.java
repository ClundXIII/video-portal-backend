package co.clund.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;
import co.clund.module.Api;
import co.clund.module.Video;
import co.clund.submodule.video.dbmodel.Platform;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
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

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json"))
				.getJSONObject("credentials");

		final DatabaseConnector submoduleConnector = l.getDbCon().getSubmoduleConnector(Video.VIDEO_LOCATION);
		Platform.populateTestPlatforms(submoduleConnector, credentialData);

		User.addNewLocalUser(l.getDbCon(), "correctUser", "correctPassword", "email@test.com", true);

		startHttpListener(l);

		logger.log(Level.INFO, "testing viewing api");

		assertTrue(httpRequestAsUser("correctUser", "correctPassword", baseUrl, baseUrl + "/" + Api.LOCATION)
				.contains("API Documentation:"));
	}
}