package co.clund.db.model;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;
import co.clund.oauth2.AbstractOAuth2UserPlatform.TokenData;

public class UserOAuth2Connection extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "user_oauth2_connection";

	public static final String DB_TABLE_COLUMN_NAME_USER_ID = "user_id";
	public static final String DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID = "oauth2_platform_id";
	public static final String DB_TABLE_COLUMN_NAME_STATES = "states";
	public static final String DB_TABLE_COLUMN_NAME_ACCESS_TOKEN = "access_token";
	public static final String DB_TABLE_COLUMN_NAME_ACCESS_TOKEN_EXPIRES = "access_token_expires";
	public static final String DB_TABLE_COLUMN_NAME_REFRESH_TOKEN = "refresh_token";
	public static final String DB_TABLE_COLUMN_NAME_REFRESH_TOKEN_EXPIRES = "refresh_token_expires";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_USER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_STATES, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_ACCESS_TOKEN, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_ACCESS_TOKEN_EXPIRES, DbValueType.TIMESTAMP);
		columnMap.put(DB_TABLE_COLUMN_NAME_REFRESH_TOKEN, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_REFRESH_TOKEN_EXPIRES, DbValueType.TIMESTAMP);
	}

	private final int id;
	private final int userId;
	private final int oAuth2PlatformId;
	private final Set<String> states;
	private final String accessToken;
	private final Date accessTokenExpires;
	private final String refreshToken;
	private final Date refreshTokenExpires;

	private final static UserOAuth2Connection dummyUserOAuth2Connection = new UserOAuth2Connection();
	
	public UserOAuth2Connection() {
		this(-1, -1, -1, null, "", null, "", null);
	}

	public UserOAuth2Connection(int id, int userId, int oAuth2PlatformId, String states, String accessToken,
			Date accessTokenExpires, String refreshToken, Date refreshTokenExpires) {
		super(DB_TABLE_NAME, columnMap);
		this.id = id;
		this.userId = userId;
		this.oAuth2PlatformId = oAuth2PlatformId;

		this.states = new HashSet<>();
		if (states != null) {
			for (String s : states.split(",")) {
				if (!s.equals("")) {
					this.states.add(s);
				}
			}
		}

		this.accessToken = accessToken;
		this.accessTokenExpires = accessTokenExpires;
		this.refreshToken = refreshToken;
		this.refreshTokenExpires = refreshTokenExpires;
	}

	private static UserOAuth2Connection getUserOAuth2ConnectionFromDbResult(Map<String, DbValue> result) {

		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int userId = result.get(DB_TABLE_COLUMN_NAME_USER_ID).getInt();
		int oAuth2PlatformId = result.get(DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID).getInt();
		String states = result.get(DB_TABLE_COLUMN_NAME_STATES).getString();
		String accessToken = result.get(DB_TABLE_COLUMN_NAME_ACCESS_TOKEN).getString();
		Date accessTokenExpires = result.get(DB_TABLE_COLUMN_NAME_ACCESS_TOKEN_EXPIRES).getTimestamp();
		String refreshToken = result.get(DB_TABLE_COLUMN_NAME_REFRESH_TOKEN).getString();
		Date refreshTokenExpires = result.get(DB_TABLE_COLUMN_NAME_REFRESH_TOKEN_EXPIRES).getTimestamp();

		return new UserOAuth2Connection(id, userId, oAuth2PlatformId, states, accessToken, accessTokenExpires,
				refreshToken, refreshTokenExpires);
	}

	public static UserOAuth2Connection getUserOAuth2ConnectionByUserPlatformId(DatabaseConnector dbCon, int userId,
			int platformId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID),
				Arrays.asList(new DbValue(userId), new DbValue(platformId)), dummyUserOAuth2Connection.getColumns());

		System.out.println(results);
		
		if (results.isEmpty()) {
			return null;
		}

		return getUserOAuth2ConnectionFromDbResult(results.get(0));
	}

	public static void addNewUserOAuth2Connection(DatabaseConnector dbCon, int userId, int oAuth2PlatformId,
			String states, TokenData clientCredData) {
		addNewUserOAuth2Connection(dbCon, userId, oAuth2PlatformId, states, clientCredData.accessToken,
				clientCredData.accessTokenExpires, clientCredData.refreshToken, clientCredData.accessTokenExpires);
	}

	public static void addNewUserOAuth2Connection(DatabaseConnector dbCon, int userId, int oAuth2PlatformId,
			String states, String accessToken, Date accessTokenExpires, String refreshToken, Date refreshTokenExpires) {

		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_OAUTH2_PLATFORM_ID,
						DB_TABLE_COLUMN_NAME_STATES, DB_TABLE_COLUMN_NAME_ACCESS_TOKEN,
						DB_TABLE_COLUMN_NAME_ACCESS_TOKEN_EXPIRES, DB_TABLE_COLUMN_NAME_REFRESH_TOKEN,
						DB_TABLE_COLUMN_NAME_REFRESH_TOKEN_EXPIRES),
				Arrays.asList(new DbValue(userId), new DbValue(oAuth2PlatformId), new DbValue(states),
						new DbValue(accessToken), new DbValue(new Timestamp(accessTokenExpires.getTime())),
						new DbValue(refreshToken), new DbValue(new Timestamp(refreshTokenExpires.getTime()))));

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

	public String getAccessToken() {
		return accessToken;
	}

	public Set<String> getStates() {
		return states;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public Date getAccessTokenExpires() {
		return accessTokenExpires;
	}

	public Date getRefreshTokenExpires() {
		return refreshTokenExpires;
	}

}
