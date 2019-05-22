package co.clund.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.DBOAuth2Platform;
import co.clund.db.model.User;
import co.clund.module.Video;
import co.clund.submodule.video.VideoIndex;
import co.clund.submodule.video.dbmodel.ExternalSubscription;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
import junit.framework.TestCase;

public class SubscriptionTest extends TestCase implements HttpTest {

	public SubscriptionTest(String testName) {
		super(testName);
	}

	public void testSub() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();
		String url = getIpAndPort(jData);

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json"))
				.getJSONObject("credentials");

		final DatabaseConnector submoduleConnector = l.getDbCon().getSubmoduleConnector(Video.VIDEO_LOCATION);
		DBOAuth2Platform.populateTestPlatforms(l.getDbCon(), credentialData);

		VideoPlatform.addNewPlatform(submoduleConnector, "yt01", "Youtube", "youtube", 1);
		VideoPlatform.addNewPlatform(submoduleConnector, "vi01", "Vimeo", "vimeo", 2);

		User.addNewLocalUser(l.getDbCon(), "correctUser", "correctPassword", "email@test.com", false);

		ExternalSubscription.addNewExternalSubscription(submoduleConnector, 1, 1, "UCNSwcDEUfIEzYdAPscXo6ZA");

		startHttpListener(l);

		httpRequest(url);

		httpRequestAsUser("correctUser", "correctPassword", url, url + "/" + Video.VIDEO_LOCATION + "/" + VideoIndex.INDEX_LOCATION);

		l.stop_join();

		assertTrue(true);
	}

}
