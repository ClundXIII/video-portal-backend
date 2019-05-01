package co.clund.submodule.core.dbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;
import co.clund.db.model.AbstractDbTable;
import co.clund.util.ResourceUtil;

public class TBlockRegionRelation extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "block_region_relation";
	public static final String DB_TABLE_COLUMN_NAME_TEMPLATE_KEY = "template_key";
	public static final String DB_TABLE_COLUMN_NAME_REGION_KEY = "region_key";
	public static final String DB_TABLE_COLUMN_NAME_BLOCK_KEY = "block_key";
	public static final String DB_TABLE_COLUMN_NAME_POSITION = "position";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_TEMPLATE_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_REGION_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_BLOCK_KEY, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_POSITION, DbValueType.INTEGER);
	}

	private final int id;

	private final String templateKey;
	private final String regionKey;
	private final String blockKey;
	private final int order;

	public static TBlockRegionRelation dummyTBlockRegionRelation = new TBlockRegionRelation();

	public TBlockRegionRelation() {
		this(-1, null, null, null, -1);
	}

	public TBlockRegionRelation(int id, String templateKey, String regionKey, String blockKey, int order) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.templateKey = templateKey;
		this.regionKey = regionKey;
		this.blockKey = blockKey;
		this.order = order;
	}

	public static void addNewTBlockRegionRelation(DatabaseConnector dbCon, String templateKey, String regionKey,
			String blockKey, int position) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_TEMPLATE_KEY, DB_TABLE_COLUMN_NAME_REGION_KEY,
						DB_TABLE_COLUMN_NAME_BLOCK_KEY, DB_TABLE_COLUMN_NAME_POSITION),
				Arrays.asList(new DbValue(templateKey), new DbValue(regionKey), new DbValue(blockKey),
						new DbValue(position)));
	}

	public static TBlockRegionRelation getTBlockRegionRelationById(DatabaseConnector dbCon, int id) {
		List<Map<String, DbValue>> result = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID, new DbValue(id),
				columnMap);

		if (result.size() == 0) {
			return null;
		}

		return getTBlockRegionRelationFromResult(result.get(0));
	}

	public static List<TBlockRegionRelation> getOrderedTBlockRegionRelationsForTemplateRegion(DatabaseConnector dbCon,
			String templateKey, String regionKey) {
		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_TEMPLATE_KEY, DB_TABLE_COLUMN_NAME_REGION_KEY),
				Arrays.asList(new DbValue(templateKey), new DbValue(regionKey)), columnMap,
				" ORDER BY " + DB_TABLE_COLUMN_NAME_POSITION + " ASC ");

		List<TBlockRegionRelation> retList = new ArrayList<>();

		for (Map<String, DbValue> result : results) {
			retList.add(getTBlockRegionRelationFromResult(result));
		}
		return retList;
	}

	private static TBlockRegionRelation getTBlockRegionRelationFromResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		String templateKey = result.get(DB_TABLE_COLUMN_NAME_TEMPLATE_KEY).getString();
		String regionKey = result.get(DB_TABLE_COLUMN_NAME_REGION_KEY).getString();
		String blockKey = result.get(DB_TABLE_COLUMN_NAME_BLOCK_KEY).getString();
		int order = result.get(DB_TABLE_COLUMN_NAME_POSITION).getInt();

		return new TBlockRegionRelation(id, templateKey, regionKey, blockKey, order);
	}

	@Override
	public int getId() {
		return id;
	}

	public static void initializeDefaultTBlockRegionRelation(DatabaseConnector dbCon) {
		for (String s : ResourceUtil.getResourceAsString("/default/default-block-region-relation.conf").split("\n")) {
			if (s.equals(""))
				continue;
			String templateKey = s.split(" ")[0];
			String regionKey = s.split(" ")[1];
			String blockKey = s.split(" ")[2];
			int position = new Integer(s.split(" ")[3]).intValue();

			addNewTBlockRegionRelation(dbCon, templateKey, regionKey, blockKey, position);
		}
	}

	public String getTemplateKey() {
		return templateKey;
	}

	public String getRegionKey() {
		return regionKey;
	}

	public String getBlockKey() {
		return blockKey;
	}

	public int getOrder() {
		return order;
	}
}
