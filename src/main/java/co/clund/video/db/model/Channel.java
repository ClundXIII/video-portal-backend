package co.clund.video.db.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.DbValue;
import co.clund.video.db.DbValueType;

public class Channel extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "channel";
	public static final String DB_TABLE_COLUMN_NAME_OWNER_ID = "owner_id";
	public static final String DB_TABLE_COLUMN_NAME_NAME = "name";
	public static final String DB_TABLE_COLUMN_NAME_PLATFORM_ID = "platform_id";
	public static final String DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER = "platform_identifier";
	public static final String DB_TABLE_COLUMN_NAME_PAYWALL_ID = "paywall_id";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_OWNER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_NAME, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_PLATFORM_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_PAYWALL_ID, DbValueType.INTEGER);
	}

	private final int id;
	private final int ownerId;
	private final String name;
	private final int platformId;
	private final String platformIdentifier;
	private final int paywallId;

	public static Channel dummyChannel = new Channel();

	public Channel() {
		this(-1, -1, null, -1, null, -1);
	}

	public Channel(int id, int ownerId, String name, int platformId, String platformIdentifier, int paywallId) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.platformId = platformId;
		this.platformIdentifier = platformIdentifier;
		this.paywallId = paywallId;
	}

	private static Channel getChannelFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int ownerId = result.get(DB_TABLE_COLUMN_NAME_OWNER_ID).getInt();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();
		int platformId = result.get(DB_TABLE_COLUMN_NAME_PLATFORM_ID).getInt();
		String patformIdentifier = result.get(DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER).getString();
		int paywallId = result.get(DB_TABLE_COLUMN_NAME_PAYWALL_ID).getInt();

		return new Channel(id, ownerId, name, platformId, patformIdentifier, paywallId);
	}

	public static Channel getChannelById(DatabaseConnector dbCon, int channelId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(channelId), dummyChannel.getColumns());

		Map<String, DbValue> result = results.get(0);

		Channel c = getChannelFromDbResult(result);

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

	public int getPlatformId() {
		return platformId;
	}

	public String getPlatformIdentifier() {
		return platformIdentifier;
	}

	public static Channel getChannelByPlatformIdAndIdentifier(DatabaseConnector dbCon, int platformId,
			String channelIdentifier) {
		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_PLATFORM_ID, DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER),
				Arrays.asList(new DbValue(platformId), new DbValue(channelIdentifier)), dummyChannel.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		Map<String, DbValue> result = results.get(0);

		Channel c = getChannelFromDbResult(result);

		return c;
	}

	public boolean isPayWalled() {
		return paywallId >= 0;
	}

	public int getPaywallId() {
		return paywallId;
	}

}
