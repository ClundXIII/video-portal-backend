package co.clund.submodule.video.dbmodel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;
import co.clund.db.model.AbstractDbTable;

public class ClientCommunityChannelAccess extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "client_community_channel_access";
	public static final String DB_TABLE_COLUMN_NAME_CLIENT_COMMUNITY_ID = "client_community_id";
	public static final String DB_TABLE_COLUMN_NAME_CHANNEL_ID = "channel_id";
	public static final String DB_TABLE_COLUMN_NAME_ACCESS = "access";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_CLIENT_COMMUNITY_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_CHANNEL_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_ACCESS, DbValueType.STRING);
	}

	private final int id;
	private final int clientCommunityId;
	private final int channelId;
	private final String access;

	public static ClientCommunityChannelAccess dummyClientCommunityChannelAccess = new ClientCommunityChannelAccess();

	public ClientCommunityChannelAccess() {
		this(-1, -1, -1, null);
	}

	public ClientCommunityChannelAccess(int id, int clientCommunityId, int channelId, String access) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.clientCommunityId = clientCommunityId;
		this.channelId = channelId;
		this.access = access;
	}

	private static ClientCommunityChannelAccess getClientCommunityChannelAccessFromDbResult(
			Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();

		int clientCommunityId = result.get(DB_TABLE_COLUMN_NAME_CLIENT_COMMUNITY_ID).getInt();
		int userId = result.get(DB_TABLE_COLUMN_NAME_CHANNEL_ID).getInt();
		String access = result.get(DB_TABLE_COLUMN_NAME_ACCESS).getString();

		return new ClientCommunityChannelAccess(id, clientCommunityId, userId, access);
	}

	public static ClientCommunityChannelAccess getClientCommunityChannelAccessById(DatabaseConnector dbCon,
			int accessId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID, new DbValue(accessId),
				dummyClientCommunityChannelAccess.getColumns());

		Map<String, DbValue> result = results.get(0);

		ClientCommunityChannelAccess c = getClientCommunityChannelAccessFromDbResult(result);

		return c;
	}

	public static ClientCommunityChannelAccess getClientCommunityChannelAccessByCommunityChannel(
			DatabaseConnector dbCon, int communityId, int channelId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_CLIENT_COMMUNITY_ID, DB_TABLE_COLUMN_NAME_CHANNEL_ID),
				Arrays.asList(new DbValue(communityId), new DbValue(channelId)),
				dummyClientCommunityChannelAccess.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		Map<String, DbValue> result = results.get(0);

		ClientCommunityChannelAccess c = getClientCommunityChannelAccessFromDbResult(result);

		return c;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getClientCommunityId() {
		return clientCommunityId;
	}

	public int getChannelId() {
		return channelId;
	}

	public String getAccess() {
		return access;
	}

}
