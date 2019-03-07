package co.clund.video.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;

import org.apache.http.client.utils.URIBuilder;

import co.clund.video.UserSession;
import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.Platform;
import co.clund.video.db.model.Video;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.html.HtmlPage;
import co.clund.video.html.HtmlResponsiveColumns;
import co.clund.video.platform.AbstractPlatform;
import co.clund.video.platform.PlatformVideo;
import co.clund.video.subscription.SubscriptionHelper;
import co.clund.video.util.cache.Cache;
import co.clund.video.util.cache.PermanentCache;

public class WatchVideo extends AbstractModule {

	public static final String LOCATION = "v";

	public static final String GET_PARAM_PLATFORM_VIDEO_KEY = "video";

	Cache<PlatformVideo> videoCache = new PermanentCache<>("video_cache");

	private final SubscriptionHelper subscriptionHelper;

	public WatchVideo(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);

		subscriptionHelper = new SubscriptionHelper(dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		try {
			PlatformVideo vid = PlatformVideo.getVideo(dbCon, parameters.get(GET_PARAM_PLATFORM_VIDEO_KEY)[0]);

			HtmlPage p = new HtmlPage(vid.getTitle() + " - Video Portal", null, null, s);

			p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

			HtmlResponsiveColumns cols = new HtmlResponsiveColumns();

			cols.startColumn(10);

			cols.writeH3(vid.getTitle());

			cols.write(vid.renderBuilder(dbCon));

			cols.writeText("Published: " + Video.UPLOAD_DATE_FORMAT.format(vid.getDate()) + " by ");

			URIBuilder channelBuilder = new URIBuilder("c");

			final Platform platformById = Platform.getPlatformById(dbCon, vid.getPlatformId());
			AbstractPlatform plat = AbstractPlatform.getPlatformFromConfig(platformById);

			channelBuilder.addParameter("id", platformById.getKey() + "_" + vid.getChannelIdentifier());

			cols.writeLink(channelBuilder.toString(), plat.getChannelName(vid.getChannelIdentifier()), false);

			cols.newLine();
			cols.writeLink(vid.getOriginalVideoLink(dbCon), "click here if the video doesnt load", true);

			cols.writeHline();
			cols.writeText(vid.getDescription());

			cols.startColumn(2);

			cols.writeText("recommended videos");

			List<HtmlGenericDiv> videoSuggestions = subscriptionHelper.renderSuggestedVideos(vid, 8);
			while (videoSuggestions.size() > 0) {
				int toRemove = (int) (Math.random() * videoSuggestions.size());

				cols.write(videoSuggestions.remove(toRemove));
			}

			p.write(cols);

			return p.finish().getBytes();

		} catch (Exception e) {
			HtmlPage p = new HtmlPage("Video Portal", null, null, s);

			logger.log(Level.WARNING, "Error while trying to load video div: " + e.getMessage());
			e.printStackTrace();
			p.writeText("video could not be loaded");

			return p.finish().getBytes();
		}

	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		return allFunctions;
	}
}
