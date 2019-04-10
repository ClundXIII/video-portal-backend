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
import co.clund.submodule.video.dbmodel.Platform;
import co.clund.util.cache.DynamicAsyncExpiringCache;
import co.clund.util.log.LoggingUtil;

public abstract class AbstractPlatform /* extends AbstractCachedQueryConnection */ {

	public static final String REMOTE_LOCATION_CONFIG_KEY_TYPE = "type";
	// public static final String UNIQUE_ID_KEY = "uniqueId";

	private static final Map<String, Class<? extends AbstractPlatform>> allAbstractPlatform = loadAbstractPlatform();

	protected final Logger logger = LoggingUtil.getDefaultLogger();

	private static Map<String, Class<? extends AbstractPlatform>> loadAbstractPlatform() {

		Logger logger = LoggingUtil.getDefaultLogger();

		Map<String, Class<? extends AbstractPlatform>> reMap = new HashMap<>();

		Reflections reflections = new Reflections("co.clund.submodule.video.platform");
		Set<Class<? extends AbstractPlatform>> allClasses = reflections.getSubTypesOf(AbstractPlatform.class);

		for (Class<? extends AbstractPlatform> c : allClasses) {
			logger.log(Level.INFO, "loading abstract Platform class " + c.getName());
			String name = null;
			try {
				Constructor<? extends AbstractPlatform> cons = c.getConstructor(Platform.class);
				AbstractPlatform r = cons.newInstance(new Object[] { null });
				name = r.getPlatformTypeName();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			logger.log(Level.INFO, "with name " + name);
			reMap.put(name, c);
		}

		return reMap;
	}

	static List<AbstractPlatform> getAllAbstractPlatform() {

		List<AbstractPlatform> retList = new ArrayList<>();

		Reflections reflections = new Reflections("co.clund.submodule.video.platform");

		Set<Class<? extends AbstractPlatform>> allClasses = reflections.getSubTypesOf(AbstractPlatform.class);

		for (Class<? extends AbstractPlatform> c : allClasses) {
			try {
				Constructor<? extends AbstractPlatform> cons = c.getConstructor(Platform.class);
				AbstractPlatform m = cons.newInstance(new Object[] { null });

				retList.add(m);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

		return retList;
	}

	public static AbstractPlatform getPlatformFromConfig(Platform stor) {
		Class<? extends AbstractPlatform> c = allAbstractPlatform.get(stor.getType());
		Logger logger = LoggingUtil.getDefaultLogger();

		try {
			Constructor<? extends AbstractPlatform> cons = c.getConstructor(Platform.class);

			return cons.newInstance(new Object[] { stor });
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while creating AbstractPlatform: " + e.getMessage());
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

	protected final Platform platform;

	AbstractPlatform(Platform platform) {
		this.platform = platform;
	}

	Platform getPlatform() {
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

	/**
	 * @return how long until client credentials need to be renewed (in seconds)
	 */
	public abstract long getClientCredentialsExpirationTime();

	public abstract URIBuilder getClientCredentialsRequestBuilder(DatabaseConnector dbCon);

	public abstract URIBuilder getClientCredentialsUploadRequestBuilder(DatabaseConnector dbCon);

	public abstract String getClientCredentialsFromCallback(Map<String, String> callBackData);

	public abstract String renewClientCredentials(String clientCredentials);

	public abstract void revokeClientCredentials(String clientCredentials);

}
