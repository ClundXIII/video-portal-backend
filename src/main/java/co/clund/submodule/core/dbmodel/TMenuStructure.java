package co.clund.submodule.core.dbmodel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;
import co.clund.db.model.AbstractDbTable;
import co.clund.util.ResourceUtil;

public class TMenuStructure extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "menu_structure";
	public static final String DB_TABLE_COLUMN_NAME_NAME = "name";
	public static final String DB_TABLE_COLUMN_NAME_KEY = "key";
	public static final String DB_TABLE_COLUMN_NAME_CONTENT = "content";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_NAME, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_CONTENT, DbValueType.STRING);
	}

	private final int id;

	private final String name;
	private final String key;
	private final String content;

	private static final TMenuStructure dummyTMenuStructure = new TMenuStructure();

	public TMenuStructure() {
		this(-1, null, null, null);
	}

	public TMenuStructure(int id, String name, String key, String content) {
		super(DB_TABLE_NAME, columnMap);

		this.name = name;
		this.key = key;
		this.content = content;

		this.id = id;
	}

	public static TMenuStructure getTMenuStructureByKey(DatabaseConnector dbCon, String key) {
		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_KEY, new DbValue(key),
				dummyTMenuStructure.getColumns());

		if (results.size() == 0) {
			return null;
		}

		return getTMenuStructureFromResult(results.get(0));
	}

	private static TMenuStructure getTMenuStructureFromResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();
		String key = result.get(DB_TABLE_COLUMN_NAME_KEY).getString();
		String content = result.get(DB_TABLE_COLUMN_NAME_CONTENT).getString();

		return new TMenuStructure(id, name, key, content);
	}

	public static void insertNewTMenuStructure(DatabaseConnector dbCon, String name, String key, String content) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_CONTENT),
				Arrays.asList(new DbValue(name), new DbValue(key), new DbValue(content)));
	}

	@Override
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public String getContent() {
		return content;
	}

	public static void initializeDefaultStructure(DatabaseConnector dbCon) {
		String config = ResourceUtil.getResourceAsString("/default/default-menu.json");

		insertNewTMenuStructure(dbCon, "Default Menu", "default", config);
	}

}
