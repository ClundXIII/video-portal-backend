package co.clai.video.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import co.clai.video.UserSession;
import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.Platform;
import co.clai.video.html.HtmlGenericDiv;
import co.clai.video.html.HtmlPage;
import co.clai.video.platform.AbstractPlatform;
import co.clai.video.platform.PlatformVideo;

public class ViewChannel extends AbstractModule {

	public static final String LOCATION = "c";

	public ViewChannel(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		String channelName = "";

		if (!parameters.containsKey("id")) {
			HtmlPage p = new HtmlPage("View Channel: " + channelName, null, null, s);
			p.writeLink("index", "main page");
			return p.finish().getBytes();
		}

		String id = parameters.get("id")[0];

		Platform plat = Platform.getPlatformByKey(dbCon, id.substring(0, id.indexOf("_")));

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(plat);

		String channelIdentifier = id.substring(id.indexOf("_") + 1);

		channelName = abPlat.getChannelName(channelIdentifier);

		HtmlPage p = new HtmlPage("View Channel: " + channelName, null, null, s);

		HtmlGenericDiv div = new HtmlGenericDiv();

		div.writeLink(abPlat.getOriginalChannelLink(channelIdentifier), "click here to view the channel \"" + channelName + "\" on " + plat.getName(), true);

		div.newLine();

		List<PlatformVideo> videos = PlatformVideo.getLatestVideos(dbCon, plat, channelIdentifier);

		for (PlatformVideo v : videos) {
			div.write(v.renderPreview(dbCon));
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
