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

public class TBlock extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "user";
	public static final String DB_TABLE_COLUMN_NAME_KEY = "key";
	public static final String DB_TABLE_COLUMN_NAME_TYPE = "type";
	public static final String DB_TABLE_COLUMN_NAME_CONTENT = "content";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_TYPE, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_CONTENT, DbValueType.BLOB);
	}

	private final int id;

	/**
	 * HTML - plain html <br/>
	 * CLASS - invoke class <br/>
	 * NATIVE - used by HtmlPage
	 */
	public enum BlockType {
		HTML, CLASS, NATIVE
	}

	private final String key;
	private final BlockType type;
	private final String content;

	public static TBlock dummyTBlock = new TBlock();

	public TBlock() {
		this(-1, null, null, null);
	}

	public TBlock(int id, String key, String type, String content) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.key = key;
		if (type == null) {
			this.type = null;
		} else {
			this.type = BlockType.valueOf(type);
		}
		this.content = content;
	}

	public static void addNewTBlock(DatabaseConnector dbCon, String key, BlockType type, String content) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_KEY, DB_TABLE_COLUMN_NAME_TYPE, DB_TABLE_COLUMN_NAME_CONTENT),
				Arrays.asList(new DbValue(key), new DbValue(type.toString()), DbValue.newBlob(content)));
	}

	public static TBlock getTBlockById(DatabaseConnector dbCon, int id) {
		List<Map<String, DbValue>> result = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID, new DbValue(id),
				columnMap);

		if (result.size() == 0) {
			return null;
		}

		return getTBlockFromResult(result.get(0));
	}

	public static TBlock getTBlockByKey(DatabaseConnector dbCon, String key) {
		List<Map<String, DbValue>> result = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_KEY, new DbValue(key),
				columnMap);

		if (result.size() == 0) {
			return null;
		}

		return getTBlockFromResult(result.get(0));
	}

	private static TBlock getTBlockFromResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		String key = result.get(DB_TABLE_COLUMN_NAME_KEY).getString();
		String type = result.get(DB_TABLE_COLUMN_NAME_TYPE).getString();
		String content = result.get(DB_TABLE_COLUMN_NAME_CONTENT).getBlobAsString();

		return new TBlock(id, key, type, content);
	}

	@Override
	public int getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public BlockType getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public static void initializeDefaultBlocks(DatabaseConnector dbCon) {
		for (String s : ResourceUtil.getResourceAsString("/default/default-blocks.conf").split("\n")) {
			if (s.equals(""))
				continue;
			String key = s.split(" ")[0];
			String typeString = s.split(" ")[1];
			BlockType type = BlockType.valueOf(typeString);
			String content = s.substring(key.length() + typeString.length() + 2);
			addNewTBlock(dbCon, key, type, content);
		}
	}
}
