package co.clund.db.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;

public class PlaylistVideoRelation extends AbstractDbTable {

	public static final String DB_TABLE_NAME = "playlist_video_relation";
	public static final String DB_TABLE_COLUMN_NAME_PLAYLIST_ID = "playlist_id";
	public static final String DB_TABLE_COLUMN_NAME_VIDEO_ID = "video_id";

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_PLAYLIST_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_VIDEO_ID, DbValueType.INTEGER);
	}

	private final int id;
	private final int playlistId;
	private final int videoId;

	public static PlaylistVideoRelation dummyPlaylistVideoRelation = new PlaylistVideoRelation();

	public PlaylistVideoRelation() {
		this(-1, -1, -1);
	}

	public PlaylistVideoRelation(int id, int playlistId, int videoId) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.playlistId = playlistId;
		this.videoId = videoId;
	}

	private static PlaylistVideoRelation getPlaylistVideoRelationFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int playlistId = result.get(DB_TABLE_COLUMN_NAME_PLAYLIST_ID).getInt();
		int videoId = result.get(DB_TABLE_COLUMN_NAME_VIDEO_ID).getInt();

		return new PlaylistVideoRelation(id, playlistId, videoId);
	}

	public static PlaylistVideoRelation getPlaylistVideoRelationById(DatabaseConnector dbCon, int relationId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(relationId), dummyPlaylistVideoRelation.getColumns());

		Map<String, DbValue> result = results.get(0);

		PlaylistVideoRelation p = getPlaylistVideoRelationFromDbResult(result);

		return p;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getPlaylistId() {
		return playlistId;
	}

	public int getVideoId() {
		return videoId;
	}

}
