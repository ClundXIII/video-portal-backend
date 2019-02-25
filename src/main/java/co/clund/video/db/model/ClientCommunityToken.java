package co.clund.video.db.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.DbValue;
import co.clund.video.db.DbValueType;

public class ClientCommunityToken extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "client_community_token";
	public static final String DB_TABLE_COLUMN_NAME_CLIENT_COMMUNTIY_ID = "client_community_id";
	public static final String DB_TABLE_COLUMN_NAME_TOKEN = "token";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_CLIENT_COMMUNTIY_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_TOKEN, DbValueType.STRING);
	}

	private final int id;
	private final int clientCommunityId;
	private final String token;

	public static ClientCommunityToken dummyClientCommunityToken = new ClientCommunityToken();

	public ClientCommunityToken() {
		this(-1, -1, null);
	}

	public ClientCommunityToken(int id, int clientCommunityId, String token) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.clientCommunityId = clientCommunityId;
		this.token = token;
	}

	private static ClientCommunityToken getClientCommunityTokenFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int clientCommunityId = result.get(DB_TABLE_COLUMN_NAME_CLIENT_COMMUNTIY_ID).getInt();
		String token = result.get(DB_TABLE_COLUMN_NAME_TOKEN).getString();

		return new ClientCommunityToken(id, clientCommunityId, token);
	}

	public static ClientCommunityToken getClientCommunityTokenById(DatabaseConnector dbCon, int channelId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(channelId), dummyClientCommunityToken.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		Map<String, DbValue> result = results.get(0);

		ClientCommunityToken c = getClientCommunityTokenFromDbResult(result);

		return c;
	}

	public static ClientCommunityToken getClientCommunityTokenByToken(DatabaseConnector dbCon, String token) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_TOKEN, new DbValue(token),
				dummyClientCommunityToken.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		Map<String, DbValue> result = results.get(0);

		ClientCommunityToken c = getClientCommunityTokenFromDbResult(result);

		return c;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getClientCommunityId() {
		return clientCommunityId;
	}

	public String getToken() {
		return token;
	}

}
