package co.clund.db.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.json.JSONObject;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;

public class DBOAuth2Platform extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "oauth2_platform";
	public static final String DB_TABLE_COLUMN_NAME_KEY = "_key";
	public static final String DB_TABLE_COLUMN_NAME_NAME = "name";
	public static final String DB_TABLE_COLUMN_NAME_TYPE = "type";
	public static final String DB_TABLE_COLUMN_NAME_CONFIG = "config";

	public static final String PLATFORM_JSON_CONFIG_RATELIMIT = "rate_limit";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_NAME, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_TYPE, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_CONFIG, DbValueType.STRING);
	}

	private final int id;
	private final String key;
	private final String name;
	private final String type;
	private final JSONObject config;

	public static DBOAuth2Platform dummyDBOAuth2Platform = new DBOAuth2Platform();

	private static final Map<String, Map<Integer, DBOAuth2Platform>> idCache = new HashMap<>();

	private static final Map<String, Map<String, DBOAuth2Platform>> keyCache = new HashMap<>();

	public DBOAuth2Platform() {
		this(-1, null, null, null, null);
	}

	public DBOAuth2Platform(int id, String key, String name, String type, String config) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.key = key;
		this.name = name;
		this.type = type;

		JSONObject tmpConfig = null;

		try {
			tmpConfig = (config == null ? null : new JSONObject(config));
		} catch (Exception e) {
			logger.log(Level.WARNING, "error while creating platform: " + e.getMessage());
		}

		this.config = tmpConfig;
	}

	private static DBOAuth2Platform getPlatformFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		String key = result.get(DB_TABLE_COLUMN_NAME_KEY).getString();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();
		String type = result.get(DB_TABLE_COLUMN_NAME_TYPE).getString();
		String config = result.get(DB_TABLE_COLUMN_NAME_CONFIG).getString();

		return new DBOAuth2Platform(id, key, name, type, config);
	}

	public static DBOAuth2Platform getPlatformById(DatabaseConnector dbCon, int platformId) {

		DBOAuth2Platform p1 = getIdCache(dbCon).get(new Integer(platformId));

		if (p1 != null) {
			return p1;
		}

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(platformId), dummyDBOAuth2Platform.getColumns());

		Map<String, DbValue> result = results.get(0);

		DBOAuth2Platform p = getPlatformFromDbResult(result);

		getIdCache(dbCon).put(new Integer(platformId), p);

		return p;
	}

	public static DBOAuth2Platform getPlatformByKey(DatabaseConnector dbCon, String key) {

		DBOAuth2Platform p1 = getKeyCache(dbCon).get(key);

		if (p1 != null) {
			return p1;
		}

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_KEY, new DbValue(key),
				dummyDBOAuth2Platform.getColumns());

		Map<String, DbValue> result = results.get(0);

		DBOAuth2Platform p = getPlatformFromDbResult(result);

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

	public static List<DBOAuth2Platform> getAllPlatform(DatabaseConnector dbCon) {

		List<DBOAuth2Platform> retList = new ArrayList<>();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, dummyDBOAuth2Platform.getColumns());

		for (Map<String, DbValue> result : results) {

			retList.add(getPlatformFromDbResult(result));
		}

		return retList;
	}

	public static void addNewPlatform(DatabaseConnector dbCon, String key, String name, String type) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_TYPE,
						DB_TABLE_COLUMN_NAME_CONFIG),
				Arrays.asList(new DbValue(key), new DbValue(name), new DbValue(type), new DbValue("{}")));
	}

	public void edit(DatabaseConnector dbCon, String newKey, String newName, String newType, String newConfig) {

		DBOAuth2Platform oldP = DBOAuth2Platform.getPlatformById(dbCon, id);

		getIdCache(dbCon).remove(new Integer(oldP.getId()));
		getKeyCache(dbCon).remove(oldP.getKey());

		dbCon.updateValue(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_TYPE,
						DB_TABLE_COLUMN_NAME_CONFIG),
				Arrays.asList(new DbValue(newKey), new DbValue(newName), new DbValue(newType),
						new DbValue(new JSONObject(newConfig).toString())),
				DB_TABLE_COLUMN_NAME_ID, new DbValue(id));

	}

	public static void populateTestPlatforms(DatabaseConnector dbCon, JSONObject credentialData) {
		final JSONObject youtubeData = credentialData.getJSONObject("youtube");
		
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_TYPE,
						DB_TABLE_COLUMN_NAME_CONFIG),
				Arrays.asList(new DbValue("Youtube"), new DbValue("yt01"), new DbValue("youtube"),
						new DbValue("{\"oauth2_client_id\":\"" + youtubeData.getString("oauth2_client_id") + "\","
								+ "\"oauth2_client_secret\":\"" + youtubeData.getString("oauth2_client_secret")
								+ "\"}")));

	}

	private static Map<Integer, DBOAuth2Platform> getIdCache(DatabaseConnector dbCon) {
		String key = dbCon.uniqueKey;

		if (!idCache.containsKey(key)) {
			idCache.put(key, new HashMap<>());
		}

		return idCache.get(key);
	}

	private static Map<String, DBOAuth2Platform> getKeyCache(DatabaseConnector dbCon) {
		String key = dbCon.uniqueKey;

		if (!keyCache.containsKey(key)) {
			keyCache.put(key, new HashMap<>());
		}

		return keyCache.get(key);
	}
}
