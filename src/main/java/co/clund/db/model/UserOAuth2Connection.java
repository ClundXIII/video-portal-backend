package co.clund.db.model;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;

public class UserOAuth2Connection extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "user_oauth2_connection";
	public static final String DB_TABLE_COLUMN_NAME_USER_ID = "username";
	public static final String DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID = "oauth2_platform_id";
	public static final String DB_TABLE_COLUMN_NAME_LAST_RENEWED = "last_renewed";
	public static final String DB_TABLE_COLUMN_NAME_ACCESS_TOKEN = "access_token";
	public static final String DB_TABLE_COLUMN_NAME_STATES = "states";
	public static final String DB_TABLE_COLUMN_NAME_REFRESH_TOKEN = "refresh_token";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_USER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_LAST_RENEWED, DbValueType.TIMESTAMP);
		columnMap.put(DB_TABLE_COLUMN_NAME_ACCESS_TOKEN, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_STATES, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_REFRESH_TOKEN, DbValueType.STRING);
	}

	private final int id;
	private final int userId;
	private final int oAuth2PlatformId;
	private final Timestamp lastRenewed;
	private final String accessToken;
	private final Set<String> states;
	private final String refreshToken;

	public UserOAuth2Connection() {
		this(-1, -1, -1, null, null, "", "");
	}

	public UserOAuth2Connection(int id, int userId, int oAuth2PlatformId, Timestamp lastRenewed, String accessToken,
			String states, String refreshToken) {
		super(DB_TABLE_NAME, columnMap);
		this.id = id;
		this.userId = userId;
		this.oAuth2PlatformId = oAuth2PlatformId;
		this.lastRenewed = lastRenewed;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;

		this.states = new HashSet<>();
		if (states != null) {
			for (String s : states.split(",")) {
				if (!s.equals("")) {
					this.states.add(s);
				}
			}
		}
	}

	private static UserOAuth2Connection getUserOAuth2ConnectionFromDbResult(Map<String, DbValue> result) {

		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int userId = result.get(DB_TABLE_COLUMN_NAME_USER_ID).getInt();
		int oAuth2PlatformId = result.get(DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID).getInt();
		Timestamp lastRenewed = result.get(DB_TABLE_COLUMN_NAME_LAST_RENEWED).getTimestamp();
		String accessToken = result.get(DB_TABLE_COLUMN_NAME_ACCESS_TOKEN).getString();
		String states = result.get(DB_TABLE_COLUMN_NAME_STATES).getString();
		String refreshToken = result.get(DB_TABLE_COLUMN_NAME_REFRESH_TOKEN).getString();

		return new UserOAuth2Connection(id, userId, oAuth2PlatformId, lastRenewed, accessToken, states, refreshToken);
	}

	public static UserOAuth2Connection getUserOAuth2ConnectionByUserPlatformId(DatabaseConnector dbCon, int userId,
			int platformId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID),
				Arrays.asList(new DbValue(userId), new DbValue(platformId)), columnMap);

		if (results.isEmpty()) {
			return null;
		}

		return getUserOAuth2ConnectionFromDbResult(results.get(0));
	}

	public static void addNewUserOAuth2Connection(DatabaseConnector dbCon, int userId, int oAuth2PlatformId,
			Timestamp lastRenewed, String accessToken, String states, String refreshToken) {

		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID,
						DB_TABLE_COLUMN_NAME_LAST_RENEWED, DB_TABLE_COLUMN_NAME_ACCESS_TOKEN,
						DB_TABLE_COLUMN_NAME_STATES, DB_TABLE_COLUMN_NAME_REFRESH_TOKEN),
				Arrays.asList(new DbValue(userId), new DbValue(oAuth2PlatformId), new DbValue(lastRenewed),
						new DbValue(accessToken), new DbValue(states), new DbValue(refreshToken)));

	}

	@Override
	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getoAuth2PlatformId() {
		return oAuth2PlatformId;
	}

	public Timestamp getLastRenewed() {
		return lastRenewed;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public Set<String> getStates() {
		return states;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

}
