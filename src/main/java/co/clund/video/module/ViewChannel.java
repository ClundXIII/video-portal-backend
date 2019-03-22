package co.clund.video.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;

import co.clund.video.UserSession;
import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.Platform;
import co.clund.video.exception.RateLimitException;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.html.HtmlPage;
import co.clund.video.platform.AbstractPlatform;
import co.clund.video.platform.PlatformVideo;
import co.clund.video.subscription.SubscriptionHelper;

public class ViewChannel extends AbstractModule {

	public static final String GET_PARAM_CHANNEL_ID = "id";
	public static final String LOCATION = "c";

	public ViewChannel(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		String channelName = "";

		if (!parameters.containsKey(GET_PARAM_CHANNEL_ID)) {
			HtmlPage p = new HtmlPage("View Channel: " + channelName, null, null, s);
			p.writeLink("index", "main page");
			return p.finish().getBytes();
		}

		String id = parameters.get(GET_PARAM_CHANNEL_ID)[0];

		Platform plat = Platform.getPlatformByKey(dbCon, id.substring(0, id.indexOf("_")));

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(plat);

		String channelIdentifier = id.substring(id.indexOf("_") + 1);

		try {
			channelName = abPlat.getCachedChannelName(channelIdentifier);
		} catch (RateLimitException e) {
			logger.log(Level.WARNING, "Error getting channel name: " + e.getMessage());
			channelName = channelIdentifier;
		}

		HtmlPage p = new HtmlPage("View Channel: " + channelName, null, null, s);

		HtmlGenericDiv div = new HtmlGenericDiv();

		div.writeLink(abPlat.getOriginalChannelLink(channelIdentifier), "click here to view the channel \"" + channelName + "\" on " + plat.getName(), true);

		div.newLine();

		List<PlatformVideo> videos = SubscriptionHelper.videoCache.retrieve(id);

		if (videos == null) {
			try {
				videos = PlatformVideo.getLatestVideos(dbCon, plat, channelIdentifier);
				SubscriptionHelper.videoCache.put(id, videos);
			} catch (RateLimitException e) {
				p.write(div);

				p.writeText("error: ratelimit reached!");
				logger.log(Level.WARNING, "error: ratelimit reached: " + e.getMessage());
				return p.finish().getBytes();
			}
		}

		for (PlatformVideo v : videos) {
			try {
				div.write(v.renderPreview(dbCon));
			} catch (RateLimitException e) {
				div.writeText("cannot load more videos, ratelimit reached!");
				logger.log(Level.WARNING, "error: ratelimit reached: " + e.getMessage());
				break;
			}
		}

		div.newLine();

		p.write(div);

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		return allFunctions;
	}
}
