package co.clai.video.db.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.DbValue;
import co.clai.video.db.DbValueType;

public class InternalSubscription extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "internal_subscription";
	public static final String DB_TABLE_COLUMN_NAME_USER_ID = "user_id";
	public static final String DB_TABLE_COLUMN_NAME_CHANNEL_ID = "channel_id";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_USER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_CHANNEL_ID, DbValueType.INTEGER);
	}

	private final int id;
	private final int userId;
	private final int channelId;

	public static InternalSubscription dummyInternalSubscription = new InternalSubscription();

	public InternalSubscription() {
		this(-1, -1, -1);
	}

	public InternalSubscription(int id, int userId, int channelId) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.userId = userId;
		this.channelId = channelId;
	}

	private static InternalSubscription getInternalSubscriptionFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int userId = result.get(DB_TABLE_COLUMN_NAME_USER_ID).getInt();
		int channelId = result.get(DB_TABLE_COLUMN_NAME_CHANNEL_ID).getInt();

		return new InternalSubscription(id, userId, channelId);
	}

	public static InternalSubscription getInternalSubscriptionById(DatabaseConnector dbCon, int subId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID, new DbValue(subId),
				dummyInternalSubscription.getColumns());

		Map<String, DbValue> result = results.get(0);

		InternalSubscription s = getInternalSubscriptionFromDbResult(result);

		return s;
	}

	public static List<InternalSubscription> getInternalSubscriptionByUserId(DatabaseConnector dbCon, int userId) {

		List<InternalSubscription> retList = new ArrayList<>();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_USER_ID,
				new DbValue(userId), dummyInternalSubscription.getColumns());

		for (Map<String, DbValue> result : results) {
			retList.add(getInternalSubscriptionFromDbResult(result));
		}
		return retList;
	}

	public static InternalSubscription getInternalSubscriptionByUserIdChannelId(DatabaseConnector dbCon, int userId,
			int channelId) {
		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_CHANNEL_ID),
				Arrays.asList(new DbValue(userId), new DbValue(channelId)), dummyInternalSubscription.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		return getInternalSubscriptionFromDbResult(results.get(0));
	}

	public static void addNewInternalSubscription(DatabaseConnector dbCon, int userId, int channelId) {

		InternalSubscription testSub = getInternalSubscriptionByUserIdChannelId(dbCon, userId, channelId);

		if (testSub != null) {
			throw new RuntimeException("already subscribed to this channel!");
		}

		dbCon.insert(DB_TABLE_NAME, Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_CHANNEL_ID),
				Arrays.asList(new DbValue(userId), new DbValue(channelId)));
	}

	@Override
	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getChannelId() {
		return channelId;
	}

}
