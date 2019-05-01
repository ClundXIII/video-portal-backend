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

public class TSiteTemplate extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "template";
	public static final String DB_TABLE_COLUMN_NAME_KEY = "key";
	public static final String DB_TABLE_COLUMN_NAME_NAME = "name";
	public static final String DB_TABLE_COLUMN_NAME_CONTENT = "content";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_NAME, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_CONTENT, DbValueType.BLOB);
	}

	private final int id;

	private final String key;
	private final String name;
	private final String content;

	public static TSiteTemplate dummyTBlock = new TSiteTemplate();

	public TSiteTemplate() {
		this(-1, null, null, null);
	}

	public TSiteTemplate(int id, String key, String name, String content) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.key = key;
		this.name = name;
		this.content = content;
	}

	public static void addNewTSiteTemplate(DatabaseConnector dbCon, String key, String name, String content) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_NAME, DB_TABLE_COLUMN_NAME_CONTENT),
				Arrays.asList(new DbValue(key), new DbValue(name), DbValue.newBlob(content)));
	}

	public static TSiteTemplate getTSiteTemplateById(DatabaseConnector dbCon, int id) {
		List<Map<String, DbValue>> result = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID, new DbValue(id),
				columnMap);

		if (result.size() == 0) {
			return null;
		}

		return getTSiteTemplateFromResult(result.get(0));
	}

	public static TSiteTemplate getTSiteTemplateByKey(DatabaseConnector dbCon, String key) {
		List<Map<String, DbValue>> result = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_KEY, new DbValue(key),
				columnMap);

		if (result.size() == 0) {
			return null;
		}

		return getTSiteTemplateFromResult(result.get(0));
	}

	private static TSiteTemplate getTSiteTemplateFromResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		String key = result.get(DB_TABLE_COLUMN_NAME_KEY).getString();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();
		String content = result.get(DB_TABLE_COLUMN_NAME_CONTENT).getBlobAsString();

		return new TSiteTemplate(id, key, name, content);
	}

	@Override
	public int getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	public static void initializeDefaultTemplate(DatabaseConnector dbCon) {
		String pageTemplateData = ResourceUtil.getResourceAsString("/default/default-page-template.html");

		addNewTSiteTemplate(dbCon, "default-template", "Default Site Template", pageTemplateData);
	}
}