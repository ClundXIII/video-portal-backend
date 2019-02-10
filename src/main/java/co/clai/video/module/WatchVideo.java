package co.clai.video.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;

import org.apache.http.client.utils.URIBuilder;

import co.clai.video.UserSession;
import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.Platform;
import co.clai.video.db.model.Video;
import co.clai.video.html.HtmlPage;
import co.clai.video.html.HtmlResponsiveColumns;
import co.clai.video.html.HtmlGenericDiv;
import co.clai.video.platform.AbstractPlatform;
import co.clai.video.platform.PlatformVideo;
import co.clai.video.subscription.SubscriptionHelper;
import co.clai.video.util.cache.Cache;
import co.clai.video.util.cache.PermanentCache;

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

		HtmlPage p = new HtmlPage("CLAI Video Portal", null, null, s);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		HtmlResponsiveColumns cols = new HtmlResponsiveColumns();

		PlatformVideo vid = null;

		cols.startColumn(10);
		try {
			vid = PlatformVideo.getVideo(dbCon, parameters.get(GET_PARAM_PLATFORM_VIDEO_KEY)[0]);

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

		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while trying to load video div: " + e.getMessage());
			e.printStackTrace();
			cols.writeText("video could not be loaded");
		}

		cols.startColumn(2);

		cols.writeText("recommended videos");

		if (vid != null) {
			List<HtmlGenericDiv> videoSuggestions = subscriptionHelper.renderSuggestedVideos(vid, 8);
			for (int i = 0; i < videoSuggestions.size(); i++) {
				int toRemove = (int) (Math.random() * videoSuggestions.size());

				cols.write(videoSuggestions.remove(toRemove));
			}
		}

		p.write(cols);

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		return allFunctions;
	}
}
