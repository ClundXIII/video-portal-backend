package co.clund.video.platform;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.reflections.Reflections;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.Platform;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.util.log.LoggingUtil;

public abstract class AbstractPlatform /* extends AbstractCachedQueryConnection */ {

	public static final String REMOTE_LOCATION_CONFIG_KEY_TYPE = "type";
	// public static final String UNIQUE_ID_KEY = "uniqueId";

	private static final Map<String, Class<? extends AbstractPlatform>> allAbstractPlatform = loadAbstractPlatform();

	protected final Logger logger = LoggingUtil.getDefaultLogger();

	private static Map<String, Class<? extends AbstractPlatform>> loadAbstractPlatform() {

		Logger logger = LoggingUtil.getDefaultLogger();

		Map<String, Class<? extends AbstractPlatform>> reMap = new HashMap<>();

		Reflections reflections = new Reflections("co.clund.video.platform");
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

		Reflections reflections = new Reflections("co.clund.video.platform");

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
			logger.log(Level.SEVERE, "error while creating AbstractPlatform", e);
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

	abstract String getPlatformTypeName();

	public abstract List<Pattern> getSubscriptionRegExps();

	public abstract String getChannelIdentifierFromUrl(String url);

	public abstract String getOriginalChannelLink(String channelIdentifier);

	abstract String getOriginalVideoLink(PlatformVideo vid);

	List<PlatformVideo> getLatestVideos(String channelIdentifier) {
		return getLatestVideos(channelIdentifier, 100);
	}

	abstract List<PlatformVideo> getLatestVideos(String channelIdentifier, int count);

	abstract PlatformVideo getVideoInfo(String identifier);

	abstract HtmlGenericDiv renderVideo(PlatformVideo vid);

	public abstract String getChannelName(String channelIdentifier);

	public abstract String getUserName(String channelIdentifier);

	public abstract String getOAuth2ConnectRedirect(DatabaseConnector dbCon);
}
