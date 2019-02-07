package co.clai.video.db.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.json.JSONObject;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.DbValue;
import co.clai.video.db.DbValueType;

public class Paywall extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "paywall";
	public static final String DB_TABLE_COLUMN_NAME_OWNER_ID = "owner_id";
	public static final String DB_TABLE_COLUMN_NAME_NAME = "name";
	public static final String DB_TABLE_COLUMN_NAME_TYPE = "type";
	public static final String DB_TABLE_COLUMN_NAME_CONFIG = "config";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_OWNER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_NAME, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_TYPE, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_CONFIG, DbValueType.STRING);
	}

	private final int id;
	private final int ownerId;
	private final String name;
	private final String type;
	private final JSONObject config;

	public static Paywall dummyPaywall = new Paywall();

	public Paywall() {
		this(-1, -1, null, null, null);
	}

	public Paywall(int id, int ownerId, String name, String type, String config) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.type = type;

		JSONObject tmpConfig = null;

		try {
			tmpConfig = new JSONObject(config);
		} catch (Exception e) {
			logger.log(Level.WARNING, "error initializing Paywall with id: " + id + ": " + e.getMessage());
		}

		this.config = tmpConfig;
	}

	private static Paywall getPaywallFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int ownerId = result.get(DB_TABLE_COLUMN_NAME_OWNER_ID).getInt();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();
		String type = result.get(DB_TABLE_COLUMN_NAME_TYPE).getString();
		String config = result.get(DB_TABLE_COLUMN_NAME_CONFIG).getString();

		return new Paywall(id, ownerId, name, type, config);
	}

	public static Paywall getPaywallById(DatabaseConnector dbCon, int channelId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(channelId), dummyPaywall.getColumns());

		Map<String, DbValue> result = results.get(0);

		Paywall c = getPaywallFromDbResult(result);

		return c;
	}

	public static void addNewPaywall(DatabaseConnector dbCon, int ownerId, String name, String type,
			JSONObject config) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_OWNER_ID, DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_TYPE,
						DB_TABLE_COLUMN_NAME_CONFIG),
				Arrays.asList(new DbValue(ownerId), new DbValue(name), new DbValue(type),
						new DbValue(config.toString())));
	}

	@Override
	public int getId() {
		return id;
	}

	public int getOwnerId() {
		return ownerId;
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

	public void setNewConfig(DatabaseConnector dbCon, String config) {
		dbCon.updateValue(DB_TABLE_NAME, Arrays.asList(DB_TABLE_COLUMN_NAME_CONFIG),
				Arrays.asList(new DbValue(new JSONObject(config).toString())), AbstractDbTable.DB_TABLE_COLUMN_NAME_ID,
				new DbValue(id));
	}
}
