package co.clai.video.platform;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.Platform;
import co.clai.video.html.HtmlGenericDiv;
import co.clai.video.util.StringStringPair;
import co.clai.video.util.cache.Cache;

public class VimeoPlatform extends AbstractPlatform {

	private final static String API_URL = "https://api.vimeo.com/";

	private static final Pattern VIMEO_GENERAL_REGEXP = Pattern.compile("vimeo\\.com/");

	public static final String PLATFORM_KEY = "vimeo";

	private static final SimpleDateFormat VIMEO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");

	private final String apiKey;

	public VimeoPlatform(Platform platform) {
		super(platform);

		String tmpApiKey = null;
		try {
			JSONObject config = platform.getConfig();

			tmpApiKey = Base64.getEncoder().encodeToString(
					(config.getString("client_id") + ":" + config.getString("client_secret")).getBytes());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while initializing Vimeo platform: " + e.getMessage());
		}

		apiKey = tmpApiKey;
	}

	@Override
	public String getPlatformTypeName() {
		return PLATFORM_KEY;
	}

	@Override
	public List<Pattern> getSubscriptionRegExps() {
		List<Pattern> retList = new ArrayList<>();

		retList.add(VIMEO_GENERAL_REGEXP);

		return retList;
	}

	@Override
	public String getChannelIdentifierFromUrl(String url) {

		String firstCut = url.replace("https://vimeo.com/", "");

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

		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "users/" + thirdCut, new ArrayList<>(),
				httpCache);

		return rawVidData.getString("uri").replace("/users/", "");
	}

	@Override
	public String getOriginalChannelLink(String channelIdentifier) {
		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "users/" + channelIdentifier,
				new ArrayList<>(), httpCache);

		String uri = rawVidData.getJSONObject("metadata").getJSONObject("connections").getJSONObject("videos")
				.getString("uri");

		uri = uri.replace("users/", "user");

		return "https://vimeo.com" + uri;
	}

	@Override
	public String getOriginalVideoLink(PlatformVideo vid) {
		return "https://vimeo.com/" + vid.getVideoIdentifier();
	}

	@Override
	public List<PlatformVideo> getLatestVideos(String channelIdentifier, int count) {
		JSONObject returnData = requestCachedJSONDataGetWithAuthToken(
				API_URL + "users/" + channelIdentifier + "/videos?direction=desc&per_page=" + count + "&sort=date",
				new ArrayList<>(), httpCache);

		List<PlatformVideo> retList = new ArrayList<>();

		JSONArray videoArr = returnData.getJSONArray("data");

		for (int i = 0; i < videoArr.length(); i++) {

			JSONObject rawVidData = videoArr.getJSONObject(i);

			String identifier = rawVidData.getString("uri").replace("/videos/", "");

			Date publishDate = new Date(System.currentTimeMillis());
			try {
				publishDate = VIMEO_DATE_FORMAT.parse(rawVidData.getString("release_time"));
			} catch (Exception e) {
				logger.log(Level.WARNING,
						"Error while parsing date for video \"" + identifier + "\": " + e.getMessage());
			}

			JSONArray thumbnailList = rawVidData.getJSONObject("pictures").getJSONArray("sizes");
			String thumbnailLink = thumbnailList.getJSONObject(0).getString("link");

			for (int j = 0; j < thumbnailList.length(); j++) {
				if (thumbnailList.getJSONObject(j).getInt("width") == 295) {
					thumbnailLink = thumbnailList.getJSONObject(j).getString("link");
					break;
				}
			}

			boolean forceNewTab = rawVidData.getJSONObject("privacy").getString("embed").equals("public");

			String description = "";

			try {
				description = rawVidData.getString("description");
			} catch (Exception e) {
				logger.log(Level.INFO, "no description: " + e.getMessage());
			}

			retList.add(new PlatformVideo(platform.getId(), publishDate, thumbnailLink, identifier, channelIdentifier, forceNewTab,
					rawVidData.getString("name"), description));
		}

		return retList;
	}

	@Override
	public PlatformVideo getVideoInfo(String identifier) {
		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "videos/" + identifier,
				new ArrayList<>(), httpCache);

		Date publishDate = new Date(System.currentTimeMillis());
		try {
			publishDate = VIMEO_DATE_FORMAT.parse(rawVidData.getString("release_time"));
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while parsing date for video \"" + identifier + "\": " + e.getMessage());
		}

		JSONArray thumbnailList = rawVidData.getJSONObject("pictures").getJSONArray("sizes");
		String thumbnailLink = thumbnailList.getJSONObject(0).getString("link");

		for (int i = 0; i < thumbnailList.length(); i++) {
			if (thumbnailList.getJSONObject(i).getInt("width") == 295) {
				thumbnailLink = thumbnailList.getJSONObject(i).getString("link");
				break;
			}
		}

		boolean forceNewTab = rawVidData.getJSONObject("privacy").getString("embed").equals("public");

		String description = "";

		String channelIdentifier = rawVidData.getJSONObject("user").getString("uri").replace("/users/", "");

		try {
			description = rawVidData.getString("description");
		} catch (Exception e) {
			logger.log(Level.INFO, "no description: " + e.getMessage());
		}

		return new PlatformVideo(platform.getId(), publishDate, thumbnailLink, identifier, channelIdentifier, forceNewTab,
				rawVidData.getString("name"), description);
	}

	@Override
	public HtmlGenericDiv renderVideo(PlatformVideo vid) {
		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "videos/" + vid.getVideoIdentifier(),
				new ArrayList<>(), httpCache);
		HtmlGenericDiv div = new HtmlGenericDiv();

		div.writeWithoutEscaping(rawVidData.getJSONObject("embed").getString("html"));

		return div;
	}

	@Override
	public String getChannelName(String channelIdentifier) {
		return getUserName(channelIdentifier);
	}

	@Override
	public String getUserName(String channelIdentifier) {
		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "users/" + channelIdentifier,
				new ArrayList<>(), httpCache);

		return rawVidData.getString("name");
	}

	private JSONObject requestCachedJSONDataGetWithAuthToken(String scriptLocation, List<StringStringPair> parameters,
			Cache<JSONObject> cache) {

		List<StringStringPair> headerParams = new ArrayList<>();
		headerParams.add(new StringStringPair("Accept", "application/vnd.vimeo.*+json; version=3.2"));
		headerParams.add(new StringStringPair("Authorization", "basic " + apiKey));

		return requestCachedJSONDataGetWithHeader(scriptLocation, parameters, headerParams, cache);
	}

	@Override
	public String getOAuth2ConnectRedirect(DatabaseConnector dbCon) {
		// TODO Auto-generated method stub
		/// TODO TO BE DONE
		return null;
	}

}