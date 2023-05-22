package co.clund.submodule.video.dbmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import co.clund.db.DatabaseConnector;
import co.clund.db.DbValue;
import co.clund.db.DbValueType;
import co.clund.db.model.AbstractDbTable;
import co.clund.util.cache.Cache;
import co.clund.util.cache.PermanentCache;
import co.clund.util.log.LoggingUtil;

public class Video extends AbstractDbTable {

	public static final SimpleDateFormat UPLOAD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

	public static final String DB_TABLE_NAME = "video";
	public static final String DB_TABLE_COLUMN_NAME_OWNER_ID = "owner_id";
	public static final String DB_TABLE_COLUMN_NAME_CHANNEL_ID = "channel_id";
	public static final String DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER = "channel_identifier";
	public static final String DB_TABLE_COLUMN_NAME_NAME = "name";
	public static final String DB_TABLE_COLUMN_NAME_PLATFORM_ID = "platform_id";
	public static final String DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER = "platform_identifier";
	public static final String DB_TABLE_COLUMN_NAME_DATE = "date";
	public static final String DB_TABLE_COLUMN_NAME_DESCRIPTION = "description";
	public static final String DB_TABLE_COLUMN_NAME_THUMBNAIL = "thumbnail";
	public static final String DB_TABLE_COLUMN_NAME_ALLOW_EMBED = "allow_embed";

	/// TODO change this to db cache
	private static Cache<Video> videoCache = new PermanentCache<>("video_db_cache");

	private final static Map<String, DbValueType> columnMap = new HashMap<>();
	{
		columnMap.put(DB_TABLE_COLUMN_NAME_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_OWNER_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_CHANNEL_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_NAME, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_PLATFORM_ID, DbValueType.INTEGER);
		columnMap.put(DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_DATE, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_DESCRIPTION, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_THUMBNAIL, DbValueType.STRING);
		columnMap.put(DB_TABLE_COLUMN_NAME_ALLOW_EMBED, DbValueType.INTEGER);
	}

	private final int id;
	private final int ownerId;
	private final int channelId;
	private final String channelIdentifier;
	private final String name;
	private final int platformId;
	private final String platformIdentifier;
	private final Date date;
	private final String description;
	private final String thumbnail;
	private final boolean allowEmbed;

	public static Video dummyVideo = new Video();

	public Video() {
		this(-1, -1, -1, null, null, -1, null, null, null, null, false);
	}

	public Video(int id, int ownerId, int channelId, String channelIdentifier, String name, int platformId, String platformIdentifier,
			String date, String description, String thumbnail, boolean allowEmbed) {
		super(DB_TABLE_NAME, columnMap);

		this.id = id;
		this.ownerId = ownerId;
		this.channelId = channelId;
		this.channelIdentifier = channelIdentifier;
		this.name = name;
		this.platformId = platformId;
		this.platformIdentifier = platformIdentifier;

		Date tmpDate = null;
		try {
			tmpDate = UPLOAD_DATE_FORMAT.parse(date);
		} catch (Exception e) {
			logger.log(Level.WARNING, "error fetching date for video: " + e.getMessage());
		}

		this.date = tmpDate;
		this.description = description;
		this.thumbnail = thumbnail;
		this.allowEmbed = allowEmbed;
	}

	public static void addNewVideo(DatabaseConnector dbCon, int ownerId, int channelId, String channelIdentifier, String name, int platformId,
			String platformIdentifier, String date, String description, String thumbnail, boolean allowEmbed) {

		List<Map<String, DbValue>> test = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_PLATFORM_ID, DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER),
				Arrays.asList(new DbValue(platformId), new DbValue(platformIdentifier)), dummyVideo.getColumns());

		if (!test.isEmpty()) {
			LoggingUtil.getDefaultLogger().log(Level.WARNING,
					"video " + platformId + ":" + platformIdentifier + " already existed!");
			throw new RuntimeException("Video already indexed!");
		}

		dbCon.insert(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_OWNER_ID, DB_TABLE_COLUMN_NAME_CHANNEL_ID, DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER, DB_TABLE_COLUMN_NAME_NAME,
						DB_TABLE_COLUMN_NAME_PLATFORM_ID, DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER,
						DB_TABLE_COLUMN_NAME_DATE, DB_TABLE_COLUMN_NAME_DESCRIPTION, DB_TABLE_COLUMN_NAME_THUMBNAIL,
						DB_TABLE_COLUMN_NAME_ALLOW_EMBED),
				Arrays.asList(new DbValue(ownerId), new DbValue(channelId), new DbValue(channelIdentifier), new DbValue(name), new DbValue(platformId),
						new DbValue(platformIdentifier), new DbValue(date), new DbValue(description),
						new DbValue(thumbnail), DbValue.newBooleanAsInteger(allowEmbed)));

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_PLATFORM_ID, DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER),
				Arrays.asList(new DbValue(platformId), new DbValue(platformIdentifier)), dummyVideo.getColumns());

		videoCache.put(getCachedVideoKey(platformId, platformIdentifier), getVideoFromDbResult(results.get(0)));

	}

	private static Video getVideoFromDbResult(Map<String, DbValue> result) {
		int id = result.get(DB_TABLE_COLUMN_NAME_ID).getInt();
		int ownerId = result.get(DB_TABLE_COLUMN_NAME_OWNER_ID).getInt();
		int channelId = result.get(DB_TABLE_COLUMN_NAME_CHANNEL_ID).getInt();
		String channelIdentifier = result.get(DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER).getString();
		String name = result.get(DB_TABLE_COLUMN_NAME_NAME).getString();
		int platformId = result.get(DB_TABLE_COLUMN_NAME_PLATFORM_ID).getInt();
		String patformIdentifier = result.get(DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER).getString();
		String date = result.get(DB_TABLE_COLUMN_NAME_DATE).getString();
		String description = result.get(DB_TABLE_COLUMN_NAME_DESCRIPTION).getString();
		String thumbnail = result.get(DB_TABLE_COLUMN_NAME_THUMBNAIL).getString();
		boolean allowEmbed = result.get(DB_TABLE_COLUMN_NAME_ALLOW_EMBED).getIntegerAsBool();

		return new Video(id, ownerId, channelId, channelIdentifier, name, platformId, patformIdentifier, date, description, thumbnail,
				allowEmbed);
	}

	public static Video getVideoById(DatabaseConnector dbCon, int channelId) {

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_ID,
				new DbValue(channelId), dummyVideo.getColumns());

		Map<String, DbValue> result = results.get(0);

		Video v = getVideoFromDbResult(result);

		return v;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public int getChannelId() {
		return channelId;
	}

	public String getChannelIdentifier() {
		return channelIdentifier;
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

	public Date getDate() {
		return date;
	}

	public static List<Video> getLatestVideosByChannelId(DatabaseConnector dbCon, int channelId, int count) {

		List<Video> retVideo = new ArrayList<>();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, DB_TABLE_COLUMN_NAME_CHANNEL_ID,
				new DbValue(channelId), dummyVideo.getColumns(),
				" ORDER BY " + DB_TABLE_COLUMN_NAME_DATE + " DESC LIMIT " + count);

		for (Map<String, DbValue> r : results) {
			retVideo.add(getVideoFromDbResult(r));
		}

		return retVideo;
	}

	public static List<Video> getLatestVideosByPlatformChannelIdentifier(DatabaseConnector dbCon,
			int platformId, String channelIdentifier, int count) {
		List<Video> retVideo = new ArrayList<>();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, Arrays.asList(DB_TABLE_COLUMN_NAME_PLATFORM_ID, DB_TABLE_COLUMN_NAME_CHANNEL_IDENTIFIER),
				Arrays.asList(new DbValue(platformId), new DbValue(channelIdentifier) ), dummyVideo.getColumns(),
				" ORDER BY " + DB_TABLE_COLUMN_NAME_DATE + " DESC LIMIT " + count);

		for (Map<String, DbValue> r : results) {
			retVideo.add(getVideoFromDbResult(r));
		}

		return retVideo;
	}

	public static List<Video> getLatestVideos(DatabaseConnector dbCon, int count) {

		List<Video> retVideo = new ArrayList<>();

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME, dummyVideo.getColumns(),
				" ORDER BY " + DB_TABLE_COLUMN_NAME_DATE + " DESC LIMIT " + count);

		for (Map<String, DbValue> r : results) {
			retVideo.add(getVideoFromDbResult(r));
		}

		return retVideo;
	}

	public static Video getVideoByPlatformIdIdentifier(DatabaseConnector dbCon, int platformId, String identifier) {
		String cacheKey = getCachedVideoKey(platformId, identifier);

		if (videoCache.contains(cacheKey)) {
			return videoCache.retrieve(cacheKey);
		}

		List<Map<String, DbValue>> results = dbCon.select(DB_TABLE_NAME,
				Arrays.asList(DB_TABLE_COLUMN_NAME_PLATFORM_ID, DB_TABLE_COLUMN_NAME_PLATFORM_IDENTIFIER),
				Arrays.asList(new DbValue(platformId), new DbValue(identifier)), dummyVideo.getColumns());

		if (results.isEmpty()) {
			videoCache.put(cacheKey, null);
			return null;
		}

		Map<String, DbValue> result = results.get(0);

		Video v = getVideoFromDbResult(result);

		videoCache.put(cacheKey, v);

		return v;
	}

	private static String getCachedVideoKey(int platformId, String identifier) {
		String cacheKey = platformId + "@" + identifier;
		return cacheKey;
	}

	public String getDescription() {
		return description;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public boolean isAllowEmbed() {
		return allowEmbed;
	}

}