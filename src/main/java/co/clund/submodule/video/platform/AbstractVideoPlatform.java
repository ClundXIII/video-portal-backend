package co.clund.submodule.video.platform;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.reflections.Reflections;

import co.clund.db.DatabaseConnector;
import co.clund.exception.RateLimitException;
import co.clund.html.HtmlGenericDiv;
import co.clund.oauth2.AbstractOAuth2UserPlatform;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.util.cache.DynamicAsyncExpiringCache;
import co.clund.util.log.LoggingUtil;

public abstract class AbstractVideoPlatform {

	public static final String REMOTE_LOCATION_CONFIG_KEY_TYPE = "type";
	// public static final String UNIQUE_ID_KEY = "uniqueId";

	private static final Map<String, Class<? extends AbstractVideoPlatform>> allAbstractPlatform = loadAbstractPlatform();

	protected final Logger logger = LoggingUtil.getDefaultLogger();

	private static Map<String, Class<? extends AbstractVideoPlatform>> loadAbstractPlatform() {

		Logger logger = LoggingUtil.getDefaultLogger();

		Map<String, Class<? extends AbstractVideoPlatform>> reMap = new HashMap<>();

		Reflections reflections = new Reflections("co.clund.submodule.video.platform");
		Set<Class<? extends AbstractVideoPlatform>> allClasses = reflections.getSubTypesOf(AbstractVideoPlatform.class);

		for (Class<? extends AbstractVideoPlatform> c : allClasses) {
			logger.log(Level.INFO, "loading abstract VideoPlatform class " + c.getName());
			String name = null;
			try {
				Constructor<? extends AbstractVideoPlatform> cons = c.getConstructor(VideoPlatform.class, AbstractOAuth2UserPlatform.class);
				AbstractVideoPlatform r = cons.newInstance(new Object[] { null, null });
				name = r.getPlatformTypeName();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			logger.log(Level.INFO, "with name " + name);
			reMap.put(name, c);
		}

		return reMap;
	}

	public static AbstractVideoPlatform getPlatformFromConfig(VideoPlatform stor,
			AbstractOAuth2UserPlatform oAuth2UserPlatform) {
		Class<? extends AbstractVideoPlatform> c = allAbstractPlatform.get(stor.getType());
		Logger logger = LoggingUtil.getDefaultLogger();

		try {
			Constructor<? extends AbstractVideoPlatform> cons = c.getConstructor(VideoPlatform.class,
					AbstractOAuth2UserPlatform.class);

			return cons.newInstance(new Object[] { stor, oAuth2UserPlatform });
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while creating AbstractVideoPlatform: " + e.getMessage());
			logger.log(Level.SEVERE, "caused by");
			e.printStackTrace();
			return null;
		}
	}

	static List<String> getAllTypes() {
		List<String> retList = new ArrayList<>();
		retList.addAll(allAbstractPlatform.keySet());
		return retList;
	}

	protected final VideoPlatform platform;
	protected final AbstractOAuth2UserPlatform oAuth2UserPlatform;

	AbstractVideoPlatform(VideoPlatform platform, AbstractOAuth2UserPlatform oAuth2UserPlatform) {
		this.platform = platform;
		this.oAuth2UserPlatform = oAuth2UserPlatform;
	}

	VideoPlatform getPlatform() {
		return platform;
	}

	public abstract String getPlatformTypeName();

	public abstract List<Pattern> getSubscriptionRegExps();

	// functions that do not need caching:

	public abstract String getOriginalChannelLink(String channelIdentifier);

	public abstract String getOriginalVideoLink(PlatformVideo vid);

	public abstract String getChannelIdentifierFromUrl(String url) throws RateLimitException;

	// these are not cached, caching takes place in subscription helper
	List<PlatformVideo> getLatestVideos(String channelIdentifier) throws RateLimitException {
		return getLatestVideos(channelIdentifier, 100);
	}

	abstract List<PlatformVideo> getLatestVideos(String channelIdentifier, int count) throws RateLimitException;

	// protected functions to be implemented by platforms
	protected abstract PlatformVideo getVideoInfo(String identifier) throws RateLimitException;

	protected abstract HtmlGenericDiv renderVideo(PlatformVideo vid) throws RateLimitException;

	protected abstract String getChannelName(String channelIdentifier) throws RateLimitException;

	protected abstract String getUserName(String channelIdentifier) throws RateLimitException;

	// functions to be used from other places, these are cached

	final static DynamicAsyncExpiringCache<PlatformVideo> getVideoCache = new DynamicAsyncExpiringCache<>(
			"getVideoCache", 60 * 60); // 1 hour

	public PlatformVideo getCachedVideoInfo(String identifier) throws RateLimitException {
		String key = this.platform.getKey() + "_" + identifier;

		PlatformVideo value = getVideoCache.retrieve(key);
		if (value != null) {
			return value;
		}

		value = getVideoInfo(identifier);
		getVideoCache.put(key, value);
		return value;
	}

	final static DynamicAsyncExpiringCache<HtmlGenericDiv> videoRenderCache = new DynamicAsyncExpiringCache<>(
			"videoRenderCache", 60 * 60); // 1 hour

	public HtmlGenericDiv renderCachedVideo(PlatformVideo vid) throws RateLimitException {

		String key = this.platform.getKey() + "_" + vid.getVideoIdentifier();

		HtmlGenericDiv value = videoRenderCache.retrieve(key);
		if (value != null) {
			return value;
		}

		value = renderVideo(vid);
		videoRenderCache.put(key, value);
		return value;
	}

	final static DynamicAsyncExpiringCache<String> channelNameCache = new DynamicAsyncExpiringCache<>(
			"channelNameCache", 50 * 24 * 60 * 60); // 50 days

	public String getCachedChannelName(String channelIdentifier) throws RateLimitException {
		String value = channelNameCache.retrieve(channelIdentifier);
		if (value != null) {
			return value;
		}

		value = getChannelName(channelIdentifier);
		channelNameCache.put(channelIdentifier, value);
		return value;
	}

	final static DynamicAsyncExpiringCache<String> userNameCache = new DynamicAsyncExpiringCache<>("userNameCache",
			50 * 24 * 60 * 60); // 50 days

	public String getCachedUserName(String channelIdentifier) throws RateLimitException {
		String value = userNameCache.retrieve(channelIdentifier);
		if (value != null) {
			return value;
		}

		value = getUserName(channelIdentifier);
		userNameCache.put(channelIdentifier, value);
		return value;
	}

	// Management functions

	public abstract URIBuilder getClientCredentialsUploadRequestBuilder(DatabaseConnector dbCon);

}
