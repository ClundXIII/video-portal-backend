package co.clund.db.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.json.JSONObject;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;

public class ClientCommunity extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "client_community";
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

	public static ClientCommunity dummyClientCommunity = new ClientCommunity();

	public ClientCommunity() {
		this(-1, -1, null, null, null);
	}

	public ClientCommunity(int id, int ownerId, String name, String type, String config) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.type = type;

		JSONObject tmpConfig = null;

		try {
			tmpConfig = new JSONObject(config);
		} catch (Exception e) {
			logger.log(Level.WARNING, "error initializing ClientCommunity with id: " + id + ": " + e.getMessage());
		}

		this.config = tmpConfig;
	}

	private static ClientCommunity getClientCommunityFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int ownerId = result.get(DB_TABLE_COLUMN_NAME_OWNER_ID).getInt();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();
		String type = result.get(DB_TABLE_COLUMN_NAME_TYPE).getString();
		String config = result.get(DB_TABLE_COLUMN_NAME_CONFIG).getString();

		return new ClientCommunity(id, ownerId, name, type, config);
	}

	public static ClientCommunity getClientCommunityById(DatabaseConnector dbCon, int channelId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(channelId), dummyClientCommunity.getColumns());

		Map<String, DbValue> result = results.get(0);

		ClientCommunity c = getClientCommunityFromDbResult(result);

		return c;
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
