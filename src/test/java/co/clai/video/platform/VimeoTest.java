package co.clai.video.platform;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.json.JSONObject;

import co.clai.video.HttpTest;
import co.clai.video.MainHttpListener;
import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.Platform;
import co.clai.video.platform.PlatformVideo;
import co.clai.video.platform.VimeoPlatform;
import co.clai.video.util.ResourceUtil;
import co.clai.video.util.log.LoggingUtil;
import junit.framework.TestCase;

public class VimeoTest extends TestCase implements HttpTest {

	public VimeoTest(String name) {
		super(name);
	}

	public void testAllFunctions() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json")).getJSONObject("credentials");

		Platform.populateTestPlatforms(l.getDbCon(), credentialData);

		final JSONObject vimeoCredentials = credentialData.getJSONObject("vimeo");

		Platform plat = new Platform(1, "test_vi", "Test Vimeo", "vimeo",
				"{\"client_id\":\"" + vimeoCredentials.getString("client_id")
						+ "\",\"client_secret\":\""
						+ vimeoCredentials.getString("client_secret") + "\"}");

		VimeoPlatform yP = new VimeoPlatform(plat);

		final String channelId = yP.getChannelIdentifierFromUrl("https://vimeo.com/wolkemedia");
		final String videoId = "200837662";

		logger.log(Level.INFO, "channelId: " + channelId);

		logger.log(Level.INFO, "Channel name: " + yP.getChannelName(channelId));
		logger.log(Level.INFO, "Channel Link: " + yP.getOriginalChannelLink(channelId));

		PlatformVideo platVid = yP.getVideoInfo(videoId);

		logger.log(Level.INFO, "Video link: " + yP.getOriginalVideoLink(platVid));
		logger.log(Level.INFO, "PlatformType: " + yP.getPlatformTypeName());
		logger.log(Level.INFO, "User name: " + yP.getUserName(channelId));
		logger.log(Level.INFO, "Latest Videos count: " + yP.getLatestVideos(channelId).size());
		logger.log(Level.INFO, "Latest 13 Videos count: " + yP.getLatestVideos(channelId, 13).size());
		for (Pattern s : yP.getSubscriptionRegExps()) {
			logger.log(Level.INFO, "Pattern for platform: " + s.pattern());
		}
		logger.log(Level.INFO, "renderVideo: " + yP.renderVideo(platVid).finish());
	}

}
