package co.clund.video.platform;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.Platform;
import co.clund.exception.RateLimitException;
import co.clund.platform.AbstractPlatform;
import co.clund.platform.PlatformVideo;
import co.clund.platform.VimeoPlatform;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
import co.clund.video.HttpTest;
import junit.framework.TestCase;

public class VimeoTest extends TestCase implements HttpTest {

	public VimeoTest(String name) {
		super(name);
	}

	public void testAllFunctions() throws RateLimitException {
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

		AbstractPlatform yP = new VimeoPlatform(plat);

		final String channelId = yP.getChannelIdentifierFromUrl("https://vimeo.com/wolkemedia");
		final String videoId = "200837662";

		logger.log(Level.INFO, "channelId: " + channelId);

		logger.log(Level.INFO, "Channel name: " + yP.getCachedChannelName(channelId));
		logger.log(Level.INFO, "Channel Link: " + yP.getOriginalChannelLink(channelId));

		PlatformVideo platVid = yP.getCachedVideoInfo(videoId);

		logger.log(Level.INFO, "Video link: " + yP.getOriginalVideoLink(platVid));
		logger.log(Level.INFO, "PlatformType: " + yP.getPlatformTypeName());
		logger.log(Level.INFO, "User name: " + yP.getCachedUserName(channelId));
		logger.log(Level.INFO, "Latest Videos count: " + PlatformVideo.getLatestVideos(l.getDbCon(), yP, channelId).size());
		logger.log(Level.INFO, "Latest 13 Videos count: " + PlatformVideo.getLatestVideos(l.getDbCon(), yP, channelId, 13).size());
		for (Pattern s : yP.getSubscriptionRegExps()) {
			logger.log(Level.INFO, "Pattern for platform: " + s.pattern());
		}
		logger.log(Level.INFO, "renderVideo: " + yP.renderCachedVideo(platVid).finish());
	}

}
