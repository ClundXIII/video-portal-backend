package co.clai.video.db.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.DbValue;
import co.clai.video.db.DbValueType;

public class Playlist extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "playlist";
	public static final String DB_TABLE_COLUMN_NAME_USER_ID = "user_id";
	public static final String DB_TABLE_COLUMN_NAME_NAME = "name";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_USER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_NAME, DbValueType.STRING);
	}

	private final int id;
	private final int userId;
	private final String name;

	public static Playlist dummyPlaylist = new Playlist();

	public Playlist() {
		this(-1, -1, null);
	}

	public Playlist(int id, int userId, String name) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.userId = userId;
		this.name = name;
	}

	private static Playlist getPlaylistFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int userId = result.get(DB_TABLE_COLUMN_NAME_USER_ID).getInt();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();

		return new Playlist(id, userId, name);
	}

	public static Playlist getPlaylistById(DatabaseConnector dbCon, int channelId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(channelId), dummyPlaylist.getColumns());

		Map<String, DbValue> result = results.get(0);

		Playlist p = getPlaylistFromDbResult(result);

		return p;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}
}
