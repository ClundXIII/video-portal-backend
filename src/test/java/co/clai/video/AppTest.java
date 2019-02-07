package co.clai.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.Platform;
import co.clai.video.util.RandomUtil;
import co.clai.video.util.ResourceUtil;
import co.clai.video.util.log.LoggingUtil;
import co.clai.video.MainHttpListener;
import junit.framework.TestCase;

/**
 * Main class for testing
 */
public class AppTest extends TestCase implements HttpTest {

	public AppTest(String testName) {
		super(testName);
	}

	public void testHttpServer() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();
		String url = getIpAndPort(jData);

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json")).getJSONObject("credentials");

		Platform.populateTestPlatforms(l.getDbCon(), credentialData);

		startHttpListener(l);

		httpRequest(url);

		l.stop_join();

		assertTrue(true);
	}

	public static void testBcrypt() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "generating Test Password:");
		logger.log(Level.INFO, BCrypt.hashpw("testLogin", BCrypt.gensalt()));
	}

	public static void testRandomString() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "generating Test Random String:");
		logger.log(Level.INFO, RandomUtil.getRandomString());

	}
}
