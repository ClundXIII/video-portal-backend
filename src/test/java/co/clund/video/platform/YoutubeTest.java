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
import co.clund.platform.YoutubePlatform;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
import co.clund.video.HttpTest;
import junit.framework.TestCase;

public class YoutubeTest extends TestCase implements HttpTest {

	public YoutubeTest(String name) {
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

		Platform plat = new Platform(1, "test_yt", "Test Youtube", "youtube", "{\"api_key\":\""
				+ credentialData.getJSONObject("youtube").getString("api_key") + "\"}");

		AbstractPlatform yP = new YoutubePlatform(plat);

		final String channelId = yP.getChannelIdentifierFromUrl("https://www.youtube.com/user/ExplosmEntertainment");
		final String videoId = "3vuMI9YakqI";

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
