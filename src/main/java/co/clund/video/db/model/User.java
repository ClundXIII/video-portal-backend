package co.clund.video.db.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.DbValue;
import co.clund.video.db.DbValueType;
import co.clund.video.util.log.LoggingUtil;

public class User extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "user";
	public static final String DB_TABLE_COLUMN_NAME_USERNAME = "username";
	public static final String DB_TABLE_COLUMN_NAME_PASSWORD = "password";
	public static final String DB_TABLE_COLUMN_NAME_EMAIL = "email";
	public static final String DB_TABLE_COLUMN_NAME_IS_ROOT = "is_root";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_USERNAME, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_PASSWORD, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_EMAIL, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_IS_ROOT, DbValueType.INTEGER);
	}

	private final boolean isRoot;

	private final String username;
	private final String encryptedPassword;
	private final String email;

	private final int id;

	public static User dummyUser = new User();

	public User() {
		this(null, -1, null, null, false);
	}

	public User(String username, int id, String encryptedPassword, String email, boolean isRoot) {

		super(DB_TABLE_NAME, columnMap);

		this.isRoot = isRoot;

		this.username = username;
		this.encryptedPassword = encryptedPassword;
		this.email = email;

		this.id = id;
	}

	public static User login(DatabaseConnector dbCon, String username, String password) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_USERNAME,
				new DbValue(username), dummyUser.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		Map<String, DbValue> result = results.get(0);

		if (BCrypt.checkpw(password, result.get(DB_TABLE_COLUMN_NAME_PASSWORD).getString())) {

			User u = getUserFromDbResult(result);

			return u;
		}

		return null;
	}

	private static User getUserFromDbResult(Map<String, DbValue> result) {
		String username = result.get(DB_TABLE_COLUMN_NAME_USERNAME).getString();
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		String encryptedPassword = result.get(DB_TABLE_COLUMN_NAME_PASSWORD).getString();
		String newEmail = result.get(DB_TABLE_COLUMN_NAME_EMAIL).getString();
		boolean isRoot = !(result.get(DB_TABLE_COLUMN_NAME_IS_ROOT).getInt() == 0);

		return new User(username, id, encryptedPassword, newEmail, isRoot);
	}

	public static User getUserById(DatabaseConnector dbCon, int userId) {

		final Logger logger = LoggingUtil.getDefaultLogger();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID, new DbValue(userId),
				dummyUser.getColumns());

		if (results.isEmpty()) {
			logger.log(Level.WARNING, "Cannot find local User with id " + userId);
			return null;
		}

		Map<String, DbValue> result = results.get(0);

		User u = getUserFromDbResult(result);

		return u;
	}

	public boolean getIsRoot() {
		return isRoot;
	}

	public String getUsername() {
		return username;
	}

	public String getEncryptedPassword() {
		return encryptedPassword;
	}

	@Override
	public int getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public static void addNewLocalUser(DatabaseConnector dbCon, String username, String password, String email,
			boolean isRoot) {
		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_USERNAME, DB_TABLE_COLUMN_NAME_PASSWORD, DB_TABLE_COLUMN_NAME_EMAIL,
						DB_TABLE_COLUMN_NAME_IS_ROOT),
				Arrays.asList(new DbValue(username), new DbValue(BCrypt.hashpw(password, BCrypt.gensalt())),
						new DbValue(email), DbValue.newBooleanAsInteger(isRoot)));
	}

	public void setNewPassword(DatabaseConnector dbCon, String newPassword) {
		dbCon.updateValue(DB_TABLE_NAME, Arrays.asList(DB_TABLE_COLUMN_NAME_PASSWORD),
				Arrays.asList(new DbValue(BCrypt.hashpw(newPassword, BCrypt.gensalt()))), DB_TABLE_COLUMN_NAME_ID,
				new DbValue(getId()));
	}

	public void setNewUsername(DatabaseConnector dbCon, String newUsername) {
		dbCon.updateValue(DB_TABLE_NAME, Arrays.asList(DB_TABLE_COLUMN_NAME_USERNAME),
				Arrays.asList(new DbValue(newUsername)), DB_TABLE_COLUMN_NAME_ID, new DbValue(getId()));
	}

	public static User getUserByName(DatabaseConnector dbCon, String username) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_USERNAME,
				new DbValue(username), dummyUser.getColumns());

		if (results.isEmpty()) {
			return null;
		}

		Map<String, DbValue> result = results.get(0);

		User u = getUserFromDbResult(result);

		return u;
	}

	public static List<User> getAllUser(DatabaseConnector dbCon) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, dummyUser.getColumns());

		List<User> retList = new ArrayList<>();

		for (Map<String, DbValue> r : results) {
			retList.add(getUserFromDbResult(r));
		}

		return retList;
	}
}
