package co.clund.submodule.video.dbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.json.JSONObject;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;
import co.clund.db.model.AbstractDbTable;
import co.clund.submodule.video.platform.AbstractVideoPlatform;

public class VideoPlatform extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "platform";
	public static final String DB_TABLE_COLUMN_NAME_KEY = "_key";
	public static final String DB_TABLE_COLUMN_NAME_NAME = "name";
	public static final String DB_TABLE_COLUMN_NAME_TYPE = "type";
	public static final String DB_TABLE_COLUMN_NAME_CONFIG = "config";
	public static final String DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID = "oauth2_plat_id";

	public static final String PLATFORM_JSON_CONFIG_RATELIMIT = "rate_limit";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_NAME, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_TYPE, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_CONFIG, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID, DbValueType.INTEGER);
	}

	private final int id;
	private final String key;
	private final String name;
	private final String type;
	private final JSONObject config;
	private final int oauth2PlatId;

	public static VideoPlatform dummyPlatform = new VideoPlatform();

	private static final Map<String, Map<Integer, VideoPlatform>> idCache = new HashMap<>();

	private static final Map<String, Map<String, VideoPlatform>> keyCache = new HashMap<>();

	public VideoPlatform() {
		this(-1, null, null, null, null, -1);
	}

	public VideoPlatform(int id, String key, String name, String type, String config, int oauth2_plat_id) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.key = key;
		this.name = name;
		this.type = type;
		this.oauth2PlatId = oauth2_plat_id;

		JSONObject tmpConfig = null;

		try {
			tmpConfig = (config == null ? null : new JSONObject(config));
		} catch (Exception e) {
			logger.log(Level.WARNING, "error while creating platform: " + e.getMessage());
		}

		this.config = tmpConfig;
	}

	private static VideoPlatform getPlatformFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		String key = result.get(DB_TABLE_COLUMN_NAME_KEY).getString();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();
		String type = result.get(DB_TABLE_COLUMN_NAME_TYPE).getString();
		String config = result.get(DB_TABLE_COLUMN_NAME_CONFIG).getString();
		int oauth2PlatId = result.get(DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID).getInt();

		return new VideoPlatform(id, key, name, type, config, oauth2PlatId);
	}

	public static VideoPlatform getPlatformById(DatabaseConnector dbCon, int platformId) {

		VideoPlatform p1 = getIdCache(dbCon).get(new Integer(platformId));

		if (p1 != null) {
			return p1;
		}

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(platformId), dummyPlatform.getColumns());

		Map<String, DbValue> result = results.get(0);

		VideoPlatform p = getPlatformFromDbResult(result);

		getIdCache(dbCon).put(new Integer(platformId), p);

		return p;
	}

	public static VideoPlatform getPlatformByKey(DatabaseConnector dbCon, String key) {

		VideoPlatform p1 = getKeyCache(dbCon).get(key);

		if (p1 != null) {
			return p1;
		}

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_KEY, new DbValue(key),
				dummyPlatform.getColumns());

		Map<String, DbValue> result = results.get(0);

		VideoPlatform p = getPlatformFromDbResult(result);

		getKeyCache(dbCon).put(key, p);

		return p;
	}

	@Override
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public JSONObject getConfig() {
		return config;
	}

	public String getKey() {
		return key;
	}

	public int getOauth2PlatId() {
		return oauth2PlatId;
	}

	public static Map<Pattern, String> getPlatformRegExps(DatabaseConnector dbCon) {
		Map<Pattern, String> retMap = new HashMap<>();

		for (VideoPlatform p : getAllPlatform(dbCon)) {
			AbstractVideoPlatform abstPlat = AbstractVideoPlatform.getPlatformFromConfig(p, null);
			for (Pattern regExp : abstPlat.getSubscriptionRegExps()) {
				retMap.put(regExp, p.getKey());
			}
		}

		return retMap;
	}

	public static List<VideoPlatform> getAllPlatform(DatabaseConnector dbCon) {

		List<VideoPlatform> retList = new ArrayList<>();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, dummyPlatform.getColumns());

		for (Map<String, DbValue> result : results) {

			retList.add(getPlatformFromDbResult(result));
		}

		return retList;
	}

	public static void addNewPlatform(DatabaseConnector dbCon, String key, String name, String type,
			int oAuth2PlatformId) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_TYPE,
						DB_TABLE_COLUMN_NAME_CONFIG, DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID),
				Arrays.asList(new DbValue(key), new DbValue(name), new DbValue(type), new DbValue("{}"),
						new DbValue(oAuth2PlatformId)));
	}

	public void edit(DatabaseConnector dbCon, String newKey, String newName, String newType, String newConfig,
			int oAuth2PlatId2) {

		VideoPlatform oldP = VideoPlatform.getPlatformById(dbCon, id);

		getIdCache(dbCon).remove(new Integer(oldP.getId()));
		getKeyCache(dbCon).remove(oldP.getKey());

		dbCon.updateValue(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_TYPE,
						DB_TABLE_COLUMN_NAME_CONFIG, DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID),
				Arrays.asList(new DbValue(newKey), new DbValue(newName), new DbValue(newType),
						new DbValue(new JSONObject(newConfig).toString()), new DbValue(oAuth2PlatId2)),
				DB_TABLE_COLUMN_NAME_ID, new DbValue(id));

	}

	private static Map<Integer, VideoPlatform> getIdCache(DatabaseConnector dbCon) {
		String key = dbCon.uniqueKey;

		if (!idCache.containsKey(key)) {
			idCache.put(key, new HashMap<>());
		}

		return idCache.get(key);
	}

	private static Map<String, VideoPlatform> getKeyCache(DatabaseConnector dbCon) {
		String key = dbCon.uniqueKey;

		if (!keyCache.containsKey(key)) {
			keyCache.put(key, new HashMap<>());
		}

		return keyCache.get(key);
	}

	public static void populateTestPlatforms(DatabaseConnector dbCon, JSONObject credentialData) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_TYPE,
						DB_TABLE_COLUMN_NAME_CONFIG),
				Arrays.asList(new DbValue("Youtube"), new DbValue("yt01"), new DbValue("youtube"), new DbValue(
						"{\"api_key\":\"" + credentialData.getJSONObject("youtube").getString("api_key") + "\"}")));

		final JSONObject vimeoData = credentialData.getJSONObject("vimeo");
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_TYPE,
						DB_TABLE_COLUMN_NAME_CONFIG),
				Arrays.asList(new DbValue("Vimeo"), new DbValue("vi01"), new DbValue("vimeo"),
						new DbValue("{\"client_id\":\"" + vimeoData.getString("client_id") + "\","
								+ "\"client_secret\":\"" + vimeoData.getString("client_secret") + "\"")));

	}

}
