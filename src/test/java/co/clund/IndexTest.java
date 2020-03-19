package co.clund;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;
import co.clund.module.Index;
import co.clund.module.Video;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
import co.clund.video.HttpTest;
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

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json"))
				.getJSONObject("video-platform-credentials");

		final DatabaseConnector submoduleConnector = l.getDbCon().getSubmoduleConnector(Video.VIDEO_LOCATION);
		VideoPlatform.populateTestPlatforms(submoduleConnector, credentialData);

		VideoPlatform.addNewPlatform(submoduleConnector, "yt01", "Youtube", "youtube", 1);
		VideoPlatform.addNewPlatform(submoduleConnector, "vi01", "Vimeo", "vimeo", 2);

		User.addNewLocalUser(l.getDbCon(), "testUser1", "asdf1234", "test@clund.co", false);

		httpRequestAsUser("testUser1", "asdf1234", baseUrl, baseUrl + "/" + Index.INDEX_LOCATION);

	}

}
