package co.clund.video.platform;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.Platform;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.html.HtmlStyleConstants;
import co.clund.video.util.StringStringPair;

public class YoutubePlatform extends AbstractPlatform {

	private static final Pattern CHANNEL_REGEXP_CHANNEL = Pattern.compile("youtube\\.com/channel/");
	private static final Pattern CHANNEL_REGEXP_USER = Pattern.compile("youtube\\.com/user/");

	private static final String OAUTH2_ENTRY = "https://accounts.google.com/o/oauth2/v2/auth";

	public static final String PLATFORM_KEY = "youtube";

	private static final SimpleDateFormat YOUTUBE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");

	private final String apiKey;

	public YoutubePlatform(Platform platform) {
		super(platform);
		String tmpApiKey = null;
		try {
			tmpApiKey = platform.getConfig().getString("api_key");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while initializing Youtube platform: " + e.getMessage());
		}

		apiKey = tmpApiKey;
	}

	@Override
	public String getPlatformTypeName() {
		return PLATFORM_KEY;
	}

	@Override
	public List<PlatformVideo> getLatestVideos(String channelIdentifier, int count) {
		List<PlatformVideo> retList = new ArrayList<>();

		List<StringStringPair> parameters = new ArrayList<>();

		parameters.add(new StringStringPair("key", apiKey));
		parameters.add(new StringStringPair("part", "snippet,contentDetails"));
		parameters.add(new StringStringPair("channelId", channelIdentifier));
		parameters.add(new StringStringPair("maxResults", (count > 50 ? 50 : count) + ""));

		JSONObject rawVidData = requestCachedJSONDataGet("https://www.googleapis.com/youtube/v3/activities", parameters,
				httpCache);

		JSONArray items = rawVidData.getJSONArray("items");

		for (int i = 0; i < items.length(); i++) {
			JSONObject thisSnippetData = items.getJSONObject(i).getJSONObject("snippet");
			if (!thisSnippetData.getString("type").equals("upload")) {
				continue;
			}

			try {
				PlatformVideo platVid = getPlatformVideoFromJsonSnippet(items.getJSONObject(i)
						.getJSONObject("contentDetails").getJSONObject("upload").getString("videoId"), thisSnippetData);

				retList.add(platVid);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error while creating platformvideo: " + e.getMessage());
				e.printStackTrace();
			}
		}

		return retList;
	}

	@Override
	public String getOriginalVideoLink(PlatformVideo vid) {
		return "https://www.youtube.com/watch?v=" + vid.getVideoIdentifier();
	}

	@Override
	public PlatformVideo getVideoInfo(String identifier) {

		try {
			List<StringStringPair> parameters = new ArrayList<>();

			parameters.add(new StringStringPair("key", apiKey));
			parameters.add(new StringStringPair("id", identifier));
			parameters.add(new StringStringPair("part", "snippet"));

			JSONObject rawVidData = requestCachedJSONDataGet("https://www.googleapis.com/youtube/v3/videos", parameters,
					httpCache);

			if (rawVidData.getJSONArray("items").length() == 0) {
				logger.log(Level.WARNING, "No video found with id " + identifier);
				return null;
			}

			JSONObject videoData = rawVidData.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");

			PlatformVideo platVid = getPlatformVideoFromJsonSnippet(identifier, videoData);

			return platVid;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while creating platformvideo: " + e.getMessage());
			return null;
		}
	}

	private PlatformVideo getPlatformVideoFromJsonSnippet(String identifier, JSONObject videoData)
			throws ParseException {
		Date date = YOUTUBE_DATE_FORMAT.parse(videoData.getString("publishedAt").substring(0, 19));
		String thumpnailLink;
		if (videoData.getJSONObject("thumbnails").has("standard")) {
			thumpnailLink = videoData.getJSONObject("thumbnails").getJSONObject("standard").getString("url");
		} else {
			thumpnailLink = videoData.getJSONObject("thumbnails").getJSONObject("default").getString("url");
		}
		boolean forceNewTab = false;
		String title = videoData.getString("title");
		String description = videoData.getString("description");
		String channelIdentifier = videoData.getString("channelId");
		int platformId = platform.getId();

		PlatformVideo platVid = new PlatformVideo(-1, -1, -1, platformId, date, thumpnailLink, identifier, channelIdentifier, forceNewTab,
				title, description, HtmlStyleConstants.DIV_CLASS_VIDEO_YOUTUBE_THUMBNAIL);
		return platVid;
	}

	@Override
	public HtmlGenericDiv renderVideo(PlatformVideo vid) {
		HtmlGenericDiv div = new HtmlGenericDiv();

		div.writeWithoutEscaping("<iframe id=\"ytplayer\" type=\"text/html\" width=\"1280\" height=\"720\""
				+ " allowfullscreen=\"allowfullscreen\"  mozallowfullscreen=\"mozallowfullscreen\""
				+ " msallowfullscreen=\"msallowfullscreen\"  oallowfullscreen=\"oallowfullscreen\""
				+ " webkitallowfullscreen=\"webkitallowfullscreen\" src=\"https://www.youtube.com/embed/"
				+ vid.getVideoIdentifier() + "\" frameborder=\"0\"></iframe>");

		return div;
	}

	@Override
	public List<Pattern> getSubscriptionRegExps() {
		List<Pattern> retList = new ArrayList<>();

		retList.add(CHANNEL_REGEXP_USER);
		retList.add(CHANNEL_REGEXP_CHANNEL);

		return retList;
	}

	@Override
	public String getChannelIdentifierFromUrl(String url) {

		if (CHANNEL_REGEXP_USER.matcher(url).find()) {

			String firstCut = url.substring(url.indexOf("user/") + 5);

			String secondCut;
			if (firstCut.contains("?")) {
				secondCut = firstCut.substring(0, firstCut.indexOf("?"));
			} else {
				secondCut = firstCut;
			}

			String thirdCut;
			if (secondCut.contains("/")) {
				thirdCut = secondCut.substring(0, secondCut.indexOf("/"));
			} else {
				thirdCut = secondCut;
			}

			List<StringStringPair> parameters = new ArrayList<>();

			parameters.add(new StringStringPair("key", apiKey));
			parameters.add(new StringStringPair("part", "id"));
			parameters.add(new StringStringPair("forUsername", thirdCut));

			JSONObject rawVidData = requestCachedJSONDataGet("https://www.googleapis.com/youtube/v3/channels",
					parameters, httpCache);

			String channelId = rawVidData.getJSONArray("items").getJSONObject(0).getString("id");

			return channelId;

		} else if (CHANNEL_REGEXP_CHANNEL.matcher(url).find()) {
			String firstCut = url.substring(url.indexOf("channel/") + 8);

			String secondCut;
			if (firstCut.contains("?")) {
				secondCut = firstCut.substring(0, firstCut.indexOf("?"));
			} else {
				secondCut = firstCut;
			}

			String thirdCut;
			if (secondCut.contains("/")) {
				thirdCut = secondCut.substring(0, secondCut.indexOf("/"));
			} else {
				thirdCut = secondCut;
			}

			return thirdCut;
		}

		return null;
	}

	@Override
	public String getOriginalChannelLink(String channelIdentifier) {
		return "https://www.youtube.com/channel/" + channelIdentifier;
	}

	@Override
	public String getChannelName(String channelIdentifier) {
		List<StringStringPair> parameters = new ArrayList<>();

		parameters.add(new StringStringPair("key", apiKey));
		parameters.add(new StringStringPair("part", "snippet"));
		parameters.add(new StringStringPair("id", channelIdentifier));

		JSONObject rawVidData = requestCachedJSONDataGet("https://www.googleapis.com/youtube/v3/channels", parameters,
				httpCache);

		String channelTitle = rawVidData.getJSONArray("items").getJSONObject(0).getJSONObject("snippet")
				.getString("title");

		return channelTitle;
	}

	@Override
	public String getUserName(String channelIdentifier) {
		return getChannelName(channelIdentifier);
	}

	@Override
	public String getOAuth2ConnectRedirect(DatabaseConnector dbCon) {
		URIBuilder builder;
		try {
			builder = new URIBuilder(OAUTH2_ENTRY);

			builder.addParameter("client_id", platform.getConfig().getString("oauth2_client_id"));
			builder.addParameter("redirect_uri", dbCon.getListener().getSiteUrl() + "/oauth2.callback");
			builder.addParameter("scope", "https://www.googleapis.com/auth/youtube.readonly");
			builder.addParameter("access_type", "offline");

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return builder.toString();
	}

}
