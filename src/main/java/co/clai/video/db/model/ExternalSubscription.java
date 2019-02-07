package co.clai.video.db.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.DbValue;
import co.clai.video.db.DbValueType;

public class ExternalSubscription extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "external_subscription";
	public static final String DB_TABLE_COLUMN_NAME_USER_ID = "user_id";
	public static final String DB_TABLE_COLUMN_NAME_PLATFORM_ID = "platform_id";
	public static final String DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER = "channel_identifier";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_USER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_PLATFORM_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER, DbValueType.STRING);
	}

	private final int id;
	private final int userId;
	private final int platformId;
	private final String channelIdentifier;

	public static ExternalSubscription dummyExternalSubscription = new ExternalSubscription();

	public ExternalSubscription() {
		this(-1, -1, -1, null);
	}

	public ExternalSubscription(int id, int userId, int platformId, String channelIdentifier) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.userId = userId;
		this.platformId = platformId;
		this.channelIdentifier = channelIdentifier;
	}

	private static ExternalSubscription getExternalSubscriptionFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int userId = result.get(DB_TABLE_COLUMN_NAME_USER_ID).getInt();
		int platformId = result.get(DB_TABLE_COLUMN_NAME_PLATFORM_ID).getInt();
		String channelIdentifier = result.get(DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER).getString();

		return new ExternalSubscription(id, userId, platformId, channelIdentifier);
	}

	public static ExternalSubscription getExternalSubscriptionById(DatabaseConnector dbCon, int relationId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(relationId), dummyExternalSubscription.getColumns());

		Map<String, DbValue> result = results.get(0);

		ExternalSubscription s = getExternalSubscriptionFromDbResult(result);

		return s;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getPlatformId() {
		return platformId;
	}

	public String getChannelIdentifier() {
		return channelIdentifier;
	}

	public static List<ExternalSubscription> getExternalSubscriptionByUserId(DatabaseConnector dbCon, int userId) {

		List<ExternalSubscription> retList = new ArrayList<>();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_USER_ID,
				new DbValue(userId), dummyExternalSubscription.getColumns());

		for (Map<String, DbValue> result : results) {
			retList.add(getExternalSubscriptionFromDbResult(result));
		}
		return retList;
	}

	public static ExternalSubscription getExternalSubscriptionByUserIdChannelId(DatabaseConnector dbCon, int userId,
			int platformId, String channelIdentifier) {
		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_PLATFORM_ID,
						DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER),
				Arrays.asList(new DbValue(userId), new DbValue(platformId), new DbValue(channelIdentifier)),
				dummyExternalSubscription.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		return getExternalSubscriptionFromDbResult(results.get(0));
	}

	public static void addNewExternalSubscription(DatabaseConnector dbCon, int userId, int platformId,
			String channelIdentifier) {

		ExternalSubscription testSub = getExternalSubscriptionByUserIdChannelId(dbCon, userId, platformId,
				channelIdentifier);

		if (testSub != null) {
			throw new RuntimeException("already subscribed to this channel!");
		}

		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_PLATFORM_ID,
						DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER),
				Arrays.asList(new DbValue(userId), new DbValue(platformId), new DbValue(channelIdentifier)));
	}

}
