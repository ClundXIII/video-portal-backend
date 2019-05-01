package co.clund.submodule.core.dbmodel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;
import co.clund.db.DbVersionUpgrader;
import co.clund.db.model.AbstractDbTable;
import co.clund.util.ResourceUtil;

public class TConfiguration extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "configuration";
	public static final String DB_TABLE_COLUMN_NAME_MODULE = "module";
	public static final String DB_TABLE_COLUMN_NAME_KEY = "key";
	public static final String DB_TABLE_COLUMN_NAME_CONTENT = "content";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_MODULE, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_CONTENT, DbValueType.STRING);
	}

	private final int id;

	private final String submodule;
	private final String key;
	private final String content;

	private static final TConfiguration dummyTConfiguration = new TConfiguration();

	public TConfiguration() {
		this(-1, null, null, null);
	}

	public TConfiguration(int id, String submodule, String key, String content) {
		super(DB_TABLE_NAME, columnMap);

		this.submodule = submodule;
		this.key = key;
		this.content = content;

		this.id = id;
	}

	public static int getDbVersion(DatabaseConnector dbCon) {
		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_MODULE, DB_TABLE_COLUMN_NAME_KEY),
				Arrays.asList(new DbValue("db"), new DbValue("version")), dummyTConfiguration.getColumns());

		return new Integer(results.get(0).get(DB_TABLE_COLUMN_NAME_CONTENT).getString()).intValue();
	}

	public static void initializeDefaultConfig(DatabaseConnector dbCon) {
		writeConfiguration(dbCon, "core", "version", "" + DbVersionUpgrader.CURRENT_DB_VERSION_NUMBER);

		for (String s : ResourceUtil.getResourceAsString("/default/default-settings.conf").split("\n")) {
			if (s.equals(""))
				continue;
			String module = s.split(" ")[0];
			String key = s.split(" ")[1];
			String value = s.substring(module.length() + key.length() + 2);
			writeConfiguration(dbCon, module, key, value);
		}
	}

	public static void writeConfiguration(DatabaseConnector dbCon, String module, String key, String value) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_MODULE, DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_CONTENT),
				Arrays.asList(new DbValue(module), new DbValue(key), new DbValue(value)));
	}

	public static TConfiguration getConfigValueByModuleAndKey(DatabaseConnector dbCon, String module, String key) {
		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_MODULE, DB_TABLE_COLUMN_NAME_KEY),
				Arrays.asList(new DbValue(module), new DbValue(key)), dummyTConfiguration.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		return getTConfigurationFromResult(results.get(0));
	}

	private static TConfiguration getTConfigurationFromResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		String submodule = result.get(DB_TABLE_COLUMN_NAME_MODULE).getString();
		String key = result.get(DB_TABLE_COLUMN_NAME_KEY).getString();
		String content = result.get(DB_TABLE_COLUMN_NAME_CONTENT).getString();

		return new TConfiguration(id, submodule, key, content);
	}

	@Override
	public int getId() {
		return id;
	}

	public String getSubmodule() {
		return submodule;
	}

	public String getKey() {
		return key;
	}

	public String getContent() {
		return content;
	}

}
