package co.clund.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.video.MainHttpListener;
import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.ExternalSubscription;
import co.clund.video.db.model.Platform;
import co.clund.video.db.model.User;
import co.clund.video.util.ResourceUtil;
import co.clund.video.util.log.LoggingUtil;
import junit.framework.TestCase;

public class IndexTest extends TestCase implements HttpTest {

	public IndexTest(String name) {
		super(name);
	}

	public void testInvokeAnonymous() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json")).getJSONObject("credentials");

		Platform.populateTestPlatforms(l.getDbCon(), credentialData);

		startHttpListener(l);

		String baseUrl = getIpAndPort(jData);

		logger.log(Level.INFO, "requesting \"\"");
		httpRequest(baseUrl);

		logger.log(Level.INFO, "requesting \"/\"");
		httpRequest(baseUrl + "/");

		logger.log(Level.INFO, "requesting \"/index\"");
		httpRequest(baseUrl + "/index");

		l.stop_join();
	}

	public void testViewSubs() {

		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();

		String baseUrl = getIpAndPort(jData);

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		startHttpListener(l);

		User.addNewLocalUser(l.getDbCon(), "testUser1", "asdf1234", "test@clai.co", false);

		ExternalSubscription.addNewExternalSubscription(l.getDbCon(), 1, 1, "UCJs1mfRk0orBF9twGXaZA2w");

		httpRequestAsUser("testUser1", "asdf1234", baseUrl, baseUrl + "/index");

	}

}
