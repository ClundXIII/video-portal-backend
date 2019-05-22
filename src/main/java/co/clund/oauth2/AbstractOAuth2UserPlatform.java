package co.clund.oauth2;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.utils.URIBuilder;
import org.reflections.Reflections;

import co.clund.db.DatabaseConnector;
import co.clund.db.model.DBOAuth2Platform;
import co.clund.util.log.LoggingUtil;

public abstract class AbstractOAuth2UserPlatform {

	protected final Logger logger = LoggingUtil.getDefaultLogger();

	private static final Map<String, Class<? extends AbstractOAuth2UserPlatform>> oAuth2PlatformMap = initializeOAuth2Map();

	private final DBOAuth2Platform dBOAuth2Platform;

	public AbstractOAuth2UserPlatform(DBOAuth2Platform dBOAuth2Platform) {
		this.dBOAuth2Platform = dBOAuth2Platform;
	}

	private static Map<String, Class<? extends AbstractOAuth2UserPlatform>> initializeOAuth2Map() {
		Logger logger = LoggingUtil.getDefaultLogger();

		Map<String, Class<? extends AbstractOAuth2UserPlatform>> retMap = new HashMap<>();

		Reflections reflections = new Reflections("co.clund.oauth2");
		Set<Class<? extends AbstractOAuth2UserPlatform>> allClasses = reflections
				.getSubTypesOf(AbstractOAuth2UserPlatform.class);

		for (Class<? extends AbstractOAuth2UserPlatform> c : allClasses) {
			logger.log(Level.INFO, "loading abstract AbstractOAuth2UserPlatform class " + c.getName());
			String name = null;
			try {
				Constructor<? extends AbstractOAuth2UserPlatform> cons = c
						.getConstructor(DBOAuth2Platform.class);
				AbstractOAuth2UserPlatform r = cons.newInstance(new Object[] { null });
				name = r.getPlatformTypeName();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			logger.log(Level.INFO, "with name " + name);
			retMap.put(name, c);
		}

		return retMap;
	}

	public static AbstractOAuth2UserPlatform getAbstractOAuth2UserPlatformFromType(DBOAuth2Platform dBOAuth2Platform) {

		Class<? extends AbstractOAuth2UserPlatform> c = oAuth2PlatformMap.get(dBOAuth2Platform.getType());
		Logger logger = LoggingUtil.getDefaultLogger();

		try {
			Constructor<? extends AbstractOAuth2UserPlatform> cons = c.getConstructor(DBOAuth2Platform.class);

			return cons.newInstance(new Object[] { dBOAuth2Platform });
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while creating AbstractOAuth2UserPlatform: " + e.getMessage());
			logger.log(Level.SEVERE, "caused by");
			e.printStackTrace();
			return null;
		}
	}

	public abstract String getPlatformTypeName();

	/**
	 * @return how long until client credentials need to be renewed (in seconds)
	 */
	public abstract long getClientCredentialsExpirationTime();

	public abstract URIBuilder getClientCredentialsRequestBuilder(DatabaseConnector dbCon);

	public abstract URIBuilder getScopelessClientCredentialsRequestBuilder(DatabaseConnector dbCon);

	public abstract String getClientCredentialsFromCallback(Map<String, String> callBackData);

	public abstract String renewClientCredentials(String clientCredentials);

	public abstract void revokeClientCredentials(String clientCredentials);

	public final DBOAuth2Platform getdBOAuth2Platform() {
		return dBOAuth2Platform;
	}
}
