package co.clund.video.platform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.Platform;
import co.clund.video.db.model.Video;
import co.clund.video.exception.RateLimitException;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.html.HtmlImg;
import co.clund.video.html.HtmlStyleConstants;
import co.clund.video.module.ViewChannel;
import co.clund.video.module.WatchVideo;
import co.clund.video.util.cache.DynamicAsyncExpiringCache;

public class PlatformVideo {

	public static final String FULL_VIDEO_KEY = "fullVideoKey";

	/// change this to access controlled expiring cache!
	private static final DynamicAsyncExpiringCache<PlatformVideo> videoCache = new DynamicAsyncExpiringCache<>("platform_video_general_cache", 24*60*60);

	private final int videoId;
	private final int userId;
	private final int channelId;

	private final int platformId;
	private final Date date;
	private final String thumbnailLink;
	private final String videoIdentifier;
	private final String channelIdentifier;
	private final boolean forceNewTab;
	private final String title;
	private final String description;
	private final String additionalCssClasses;

	public PlatformVideo(int platformId, Date date, String thumbnailLink, String videoIdentifier, String channelIdentifier, boolean forceNewTab,
			String title, String description) {
		this(-1, -1, -1, platformId, date, thumbnailLink, videoIdentifier, channelIdentifier, forceNewTab, title, description);
	}

	public PlatformVideo(Video video) {
		this(video.getId(), video.getOwnerId(), video.getChannelId(), video.getPlatformId(), video.getDate(),
				video.getThumbnail(), video.getPlatformIdentifier(), video.getChannelIdentifier(), video.isAllowEmbed(), video.getName(),
				video.getDescription());
	}

	public PlatformVideo(int videoId, int userId, int channelId, int platformId, Date date, String thumbnailLink,
			String videoIdentifier, String channelIdentifier, boolean forceNewTab, String title, String description) {
		this(videoId, userId, channelId, platformId, date, thumbnailLink, videoIdentifier, channelIdentifier, forceNewTab, title,
				description, "");
	}

	public PlatformVideo(int videoId, int userId, int channelId, int platformId, Date date, String thumbnailLink,
			String videoIdentifier, String channelIdentifier, boolean forceNewTab, String title, String description,
			String additionalCssClasses) {
		this.userId = userId;
		this.videoId = videoId;
		this.channelId = channelId;
		this.platformId = platformId;
		this.date = date;
		this.thumbnailLink = thumbnailLink;
		this.videoIdentifier = videoIdentifier;
		this.forceNewTab = forceNewTab;
		this.title = title;
		this.description = description;
		this.additionalCssClasses = additionalCssClasses;
		this.channelIdentifier = channelIdentifier;
	}

	public String getDescription() {
		return description;
	}

	public boolean isInternal() {
		return videoId > 0;
	}

	public int getVideoId() {
		return videoId;
	}

	public int getUserId() {
		return userId;
	}

	public int getChannelId() {
		return channelId;
	}

	public int getPlatformId() {
		return platformId;
	}

	public Date getDate() {
		return date;
	}

	public String getThumbnailLink() {
		return thumbnailLink;
	}

	public String getVideoIdentifier() {
		return videoIdentifier;
	}

	public String getChannelIdentifier() {
		return channelIdentifier;
	}

	public boolean isForceNewTab() {
		return forceNewTab;
	}

	public String getTitle() {
		return title;
	}

	public String getAdditionalCssClasses() {
		return additionalCssClasses;
	}

	private static PlatformVideo getCacheVideo(DatabaseConnector dbCon, AbstractPlatform abPlat, String identifier) throws RateLimitException {

		if (videoCache.contains(identifier)) {
			return videoCache.retrieve(identifier);
		}

		Video v = Video.getVideoByPlatformIdIdentifier(dbCon, abPlat.getPlatform().getId(), identifier);

		PlatformVideo platVid;

		if (v == null) {
			platVid = abPlat.getVideoInfo(identifier);

		} else {
			platVid = new PlatformVideo(v);
		}

		videoCache.put(identifier, platVid);

		return platVid;
	}

	public JSONObject toJSONObject(DatabaseConnector dbCon) {

		JSONObject retObj = new JSONObject();

		retObj.put("videoId", videoId + "");
		retObj.put("userId", userId + "");
		retObj.put("platformId", platformId + "");
		retObj.put("date", Video.UPLOAD_DATE_FORMAT.format(date));
		retObj.put("thumbnailLink", thumbnailLink);
		retObj.put("videoIdentifier", videoIdentifier);
		retObj.put("forceNewTab", forceNewTab + "");
		retObj.put("title", title);
		retObj.put("description", description);

		String fullVideoKey = Platform.getPlatformById(dbCon, platformId).getKey() + "_" + videoIdentifier;

		retObj.put(FULL_VIDEO_KEY, fullVideoKey);

		return retObj;
	}

	private String getFullIdentifier(DatabaseConnector dbCon) {
		Platform p = Platform.getPlatformById(dbCon, platformId);

		return p.getKey() + "_" + videoIdentifier;
	}

	public static PlatformVideo getVideo(DatabaseConnector dbCon, String fullVideoKey) throws RateLimitException {
		String platformKey = fullVideoKey.substring(0, fullVideoKey.indexOf("_"));

		String videoIdent = fullVideoKey.substring(fullVideoKey.indexOf("_") + 1);

		Platform plat = Platform.getPlatformByKey(dbCon, platformKey);

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(plat);

		return PlatformVideo.getCacheVideo(dbCon, abPlat, videoIdent);
	}

	public String render(DatabaseConnector dbCon) throws RateLimitException {
		return renderBuilder(dbCon).finish();
	}

	public HtmlGenericDiv renderBuilder(DatabaseConnector dbCon) throws RateLimitException {
		Platform platform = Platform.getPlatformById(dbCon, platformId);

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(platform);

		return abPlat.renderVideo(this);
	}

	public static List<PlatformVideo> getLatestVideos(DatabaseConnector dbCon, int platformId,
			String channelIdentifier) throws RateLimitException {

		Platform p = Platform.getPlatformById(dbCon, platformId);

		return getLatestVideos(dbCon, p, channelIdentifier);
	}

	public static List<PlatformVideo> getLatestVideos(DatabaseConnector dbCon, Platform p,
			String channelIdentifier) throws RateLimitException {

		AbstractPlatform ap = AbstractPlatform.getPlatformFromConfig(p);

		List<PlatformVideo> videos = ap.getLatestVideos(channelIdentifier);

		List<PlatformVideo> retVideos = new ArrayList<>();

		for (PlatformVideo pV : videos) {

			if (videoCache.contains(pV.getFullIdentifier(dbCon))) {
				PlatformVideo tmpV = videoCache.retrieve(pV.getFullIdentifier(dbCon));

				if (tmpV.getVideoId() < 0) {
					if (!(pV.getDescription().equals(tmpV.getDescription()) && pV.getTitle().equals(tmpV.getTitle()))) {
						videoCache.put(pV.getFullIdentifier(dbCon), pV);
					}
					retVideos.add(pV);
				} else {
					retVideos.add(tmpV);
				}
			} else {
				Video v = Video.getVideoByPlatformIdIdentifier(dbCon, p.getId(), pV.getVideoIdentifier());

				PlatformVideo platVid;

				if (v == null) {
					platVid = ap.getVideoInfo(pV.getVideoIdentifier());

				} else {
					platVid = new PlatformVideo(v);
				}

				videoCache.put(platVid.getFullIdentifier(dbCon), platVid);

				retVideos.add(platVid);
			}
		}

		return retVideos;
	}

	public static List<PlatformVideo> getLatestVideos(DatabaseConnector dbCon, int platformId, String channelIdentifier,
			int amount) throws RateLimitException {

		Platform p = Platform.getPlatformById(dbCon, platformId);

		AbstractPlatform ap = AbstractPlatform.getPlatformFromConfig(p);

		return ap.getLatestVideos(channelIdentifier, amount);
	}

	public HtmlGenericDiv renderPreview(DatabaseConnector dbCon) throws RateLimitException {
		Platform p = Platform.getPlatformById(dbCon, platformId);

		HtmlGenericDiv div = new HtmlGenericDiv("", HtmlStyleConstants.DIV_CLASS_VIDEO_PREVIEW);

		div.writeLink(
				WatchVideo.LOCATION + "?" + WatchVideo.GET_PARAM_PLATFORM_VIDEO_KEY + "=" + p.getKey() + "_"
						+ videoIdentifier,
				new HtmlImg(thumbnailLink, "",
						HtmlStyleConstants.DIV_CLASS_VIDEO_THUMBNAIL + " " + additionalCssClasses),
				false);

		HtmlGenericDiv titleDiv = new HtmlGenericDiv("", HtmlStyleConstants.DIV_CLASS_VIDEO_THUMBNAIL_TITLE);
		titleDiv.writeText(title);
		div.write(titleDiv);

		HtmlGenericDiv channelDiv = new HtmlGenericDiv("", HtmlStyleConstants.DIV_CLASS_VIDEO_CHANNEL_LINK);

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(p);

		channelDiv.writeLink(ViewChannel.LOCATION + "?" + ViewChannel.GET_PARAM_CHANNEL_ID + "=" + p.getKey() + "_" + channelIdentifier, abPlat.getCachedChannelName(channelIdentifier));
		div.write(channelDiv);

		return div;
	}

	public String getOriginalVideoLink(DatabaseConnector dbCon) {
		Platform p = Platform.getPlatformById(dbCon, platformId);

		AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(p);

		return abPlat.getOriginalVideoLink(this);
	}

}
