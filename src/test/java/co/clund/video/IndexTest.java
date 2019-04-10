package co.clund.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;
import co.clund.module.Video;
import co.clund.submodule.video.VideoIndex;
import co.clund.submodule.video.dbmodel.ExternalSubscription;
import co.clund.submodule.video.dbmodel.Platform;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
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

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json"))
				.getJSONObject("credentials");

		Platform.populateTestPlatforms(l.getDbCon().getSubmoduleConnector(Video.VIDEO_LOCATION), credentialData);

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

		final DatabaseConnector submoduleConnector = l.getDbCon().getSubmoduleConnector(Video.VIDEO_LOCATION);
		Platform.populateTestPlatforms(submoduleConnector, getTestCredentials().getJSONObject("credentials"));

		User.addNewLocalUser(l.getDbCon(), "testUser1", "asdf1234", "test@clund.co", false);

		ExternalSubscription.addNewExternalSubscription(submoduleConnector, 1, 1, "UCJs1mfRk0orBF9twGXaZA2w");

		httpRequestAsUser("testUser1", "asdf1234", baseUrl,
				baseUrl + "/" + Video.VIDEO_LOCATION + "/" + VideoIndex.INDEX_LOCATION);

	}

}
