package co.clund.db.model;

import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;

public class PaywallPass extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "paywall_pass";
	public static final String DB_TABLE_COLUMN_NAME_USER_ID = "user_id";
	public static final String DB_TABLE_COLUMN_NAME_PAYWALL_ID = "paywall_id";
	public static final String DB_TABLE_COLUMN_NAME_EXPIRE = "expire";

	private static final DateTimeFormatter EXPIRE_DATA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-kk-mm-ss");

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_USER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_PAYWALL_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_EXPIRE, DbValueType.STRING);
	}

	private final int id;
	private final int userId;
	private final int paywallId;
	private final LocalDateTime expire;

	public static PaywallPass dummyPaywallPass = new PaywallPass();

	public PaywallPass() {
		this(-1, -1, -1, null);
	}

	public PaywallPass(int id, int userId, int paywallId, String expire) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.userId = userId;
		this.paywallId = paywallId;

		LocalDateTime tmpDate = null;

		try {
			tmpDate = LocalDateTime.from(EXPIRE_DATA_FORMAT.parse(expire));
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while initializing Date for Paywall pass " + id + ": " + e.getMessage());
		}

		this.expire = tmpDate;

	}

	private static PaywallPass getPaywallPassFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int userId = result.get(DB_TABLE_COLUMN_NAME_USER_ID).getInt();
		int channelId = result.get(DB_TABLE_COLUMN_NAME_PAYWALL_ID).getInt();
		String expire = result.get(DB_TABLE_COLUMN_NAME_EXPIRE).getString();

		return new PaywallPass(id, userId, channelId, expire);
	}

	public static PaywallPass getPaywallPassById(DatabaseConnector dbCon, int subId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID, new DbValue(subId),
				dummyPaywallPass.getColumns());

		Map<String, DbValue> result = results.get(0);

		PaywallPass s = getPaywallPassFromDbResult(result);

		return s;
	}

	public static List<PaywallPass> getPaywallPassByUserId(DatabaseConnector dbCon, int userId) {

		List<PaywallPass> retList = new ArrayList<>();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_USER_ID,
				new DbValue(userId), dummyPaywallPass.getColumns());

		for (Map<String, DbValue> result : results) {
			retList.add(getPaywallPassFromDbResult(result));
		}
		return retList;
	}

	public static PaywallPass getPaywallPassByUserIdPaywallId(DatabaseConnector dbCon, int userId, int paywallId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_PAYWALL_ID),
				Arrays.asList(new DbValue(userId), new DbValue(paywallId)), dummyPaywallPass.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		return getPaywallPassFromDbResult(results.get(0));
	}

	public static void extendPaywallPass(DatabaseConnector dbCon, int userId, int paywallId, Period length) {

		PaywallPass pass = getPaywallPassByUserIdPaywallId(dbCon, userId, paywallId);

		if (pass == null) {
			addNewPaywallPass(dbCon, userId, paywallId, LocalDateTime.now().plus(length));
		} else {
			dbCon.updateValue(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_EXPIRE,
					new DbValue(EXPIRE_DATA_FORMAT.format(pass.getExpire().plus(length))), DB_TABLE_COLUMN_NAME_ID,
					new DbValue(pass.getId()));
		}

	}

	private static void addNewPaywallPass(DatabaseConnector dbCon, int userId, int paywallId, LocalDateTime expire) {

		PaywallPass testSub = getPaywallPassByUserIdPaywallId(dbCon, userId, paywallId);

		if (testSub != null) {
			throw new RuntimeException("already subscribed to this channel!");
		}

		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USER_ID, DB_TABLE_COLUMN_NAME_PAYWALL_ID,
						DB_TABLE_COLUMN_NAME_EXPIRE),
				Arrays.asList(new DbValue(userId), new DbValue(paywallId),
						new DbValue(EXPIRE_DATA_FORMAT.format(expire))));
	}

	@Override
	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getPaywallId() {
		return paywallId;
	}

	public LocalDateTime getExpire() {
		return expire;
	}

}
