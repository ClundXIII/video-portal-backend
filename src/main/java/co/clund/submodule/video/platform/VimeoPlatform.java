package co.clund.submodule.video.platform;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import co.clund.db.DatabaseConnector;
import co.clund.html.HtmlGenericDiv;
import co.clund.oauth2.AbstractOAuth2UserPlatform;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.util.HttpRequestUtil;

public class VimeoPlatform extends AbstractVideoPlatform {

	private final static String API_URL = "https://api.vimeo.com/";

	private static final Pattern VIMEO_GENERAL_REGEXP = Pattern.compile("vimeo\\.com/");

	public static final String PLATFORM_KEY = "vimeo";

	private static final SimpleDateFormat VIMEO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");

	private final String apiKey;

	public VimeoPlatform(VideoPlatform platform, AbstractOAuth2UserPlatform oAuth2UserPlatform) {
		super(platform, oAuth2UserPlatform);

		String tmpApiKey = null;
		try {
			JSONObject config = oAuth2UserPlatform.getdBOAuth2Platform().getConfig();

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

		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "users/" + thirdCut);

		return rawVidData.getString("uri").replace("/users/", "");
	}

	@Override
	public String getOriginalChannelLink(String channelIdentifier) {
		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "users/" + channelIdentifier);

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
				API_URL + "users/" + channelIdentifier + "/videos?direction=desc&per_page=" + count + "&sort=date");

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

			retList.add(new PlatformVideo(platform.getId(), publishDate, thumbnailLink, identifier, channelIdentifier,
					forceNewTab, rawVidData.getString("name"), description));
		}

		return retList;
	}

	@Override
	public PlatformVideo getVideoInfo(String identifier) {
		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "videos/" + identifier);

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

		return new PlatformVideo(platform.getId(), publishDate, thumbnailLink, identifier, channelIdentifier,
				forceNewTab, rawVidData.getString("name"), description);
	}

	@Override
	public HtmlGenericDiv renderVideo(PlatformVideo vid) {
		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "videos/" + vid.getVideoIdentifier());
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
		JSONObject rawVidData = requestCachedJSONDataGetWithAuthToken(API_URL + "users/" + channelIdentifier);

		return rawVidData.getString("name");
	}

	private JSONObject requestCachedJSONDataGetWithAuthToken(String scriptLocation) {

		Map<String, String> headerParams = new HashMap<>();
		headerParams.put("Accept", "application/vnd.vimeo.*+json; version=3.2");
		headerParams.put("Authorization", "basic " + apiKey);

		return new JSONObject(
				new String(HttpRequestUtil.httpGetRequestWithHeader(scriptLocation, new HashMap<>(), headerParams)));
	}

	@Override
	public URIBuilder getClientCredentialsUploadRequestBuilder(DatabaseConnector dbCon) {
		// TODO Auto-generated method stub
		return null;
	}

}