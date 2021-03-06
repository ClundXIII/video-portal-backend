package co.clund.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.module.Video;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.util.RandomUtil;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
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

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json"))
				.getJSONObject("video-platform-credentials");

		final DatabaseConnector submoduleConnector = l.getDbCon().getSubmoduleConnector(Video.VIDEO_LOCATION);
		VideoPlatform.populateTestPlatforms(submoduleConnector, credentialData);
		
		VideoPlatform.addNewPlatform(submoduleConnector, "yt01", "Youtube", "youtube", 1);		
		VideoPlatform.addNewPlatform(submoduleConnector, "vi01", "Vimeo", "vimeo", 2);

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
