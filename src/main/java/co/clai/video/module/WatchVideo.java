package co.clai.video.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;

import co.clai.video.UserSession;
import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.Video;
import co.clai.video.html.HtmlPage;
import co.clai.video.html.HtmlResponsiveColumns;
import co.clai.video.platform.PlatformVideo;
import co.clai.video.util.cache.Cache;
import co.clai.video.util.cache.PermanentCache;

public class WatchVideo extends AbstractModule {

	public static final String LOCATION = "v";

	public static final String GET_PARAM_PLATFORM_VIDEO_KEY = "video";

	Cache<PlatformVideo> videoCache = new PermanentCache<>("video_cache");

	public WatchVideo(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage p = new HtmlPage("CLAI Video Portal", null, null, s);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		HtmlResponsiveColumns cols = new HtmlResponsiveColumns();

		cols.startColumn(10);
		try {
			PlatformVideo vid = PlatformVideo.getVideo(dbCon, parameters.get(GET_PARAM_PLATFORM_VIDEO_KEY)[0]);

			cols.writeH3(vid.getTitle());

			cols.write(vid.renderBuilder(dbCon));

			cols.writeText("Published: " + Video.UPLOAD_DATE_FORMAT.format(vid.getDate())
					+ " by <user/channel link will be added here>");
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

		p.write(cols);

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		return allFunctions;
	}
}
