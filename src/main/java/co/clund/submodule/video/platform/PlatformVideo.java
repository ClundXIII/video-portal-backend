package co.clund.submodule.video.platform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import co.clund.db.DatabaseConnector;
import co.clund.db.model.DBOAuth2Platform;
import co.clund.exception.RateLimitException;
import co.clund.html.HtmlGenericDiv;
import co.clund.html.HtmlImg;
import co.clund.html.HtmlStyleConstants;
import co.clund.oauth2.AbstractOAuth2UserPlatform;
import co.clund.submodule.video.ViewChannel;
import co.clund.submodule.video.WatchVideo;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.submodule.video.dbmodel.Video;
import co.clund.util.cache.DynamicAsyncExpiringCache;

public class PlatformVideo {

	public static final String FULL_VIDEO_KEY = "fullVideoKey";

	/// change this to access controlled expiring cache!
	private static final DynamicAsyncExpiringCache<PlatformVideo> videoCache = new DynamicAsyncExpiringCache<>(
			"platform_video_general_cache", 24 * 60 * 60);

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

	public PlatformVideo(int platformId, Date date, String thumbnailLink, String videoIdentifier,
			String channelIdentifier, boolean forceNewTab, String title, String description) {
		this(-1, -1, -1, platformId, date, thumbnailLink, videoIdentifier, channelIdentifier, forceNewTab, title,
				description);
	}

	public PlatformVideo(Video video) {
		this(video.getId(), video.getOwnerId(), video.getChannelId(), video.getPlatformId(), video.getDate(),
				video.getThumbnail(), video.getPlatformIdentifier(), video.getChannelIdentifier(), video.isAllowEmbed(),
				video.getName(), video.getDescription());
	}

	public PlatformVideo(int videoId, int userId, int channelId, int platformId, Date date, String thumbnailLink,
			String videoIdentifier, String channelIdentifier, boolean forceNewTab, String title, String description) {
		this(videoId, userId, channelId, platformId, date, thumbnailLink, videoIdentifier, channelIdentifier,
				forceNewTab, title, description, "");
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

	private static PlatformVideo getCacheVideo(DatabaseConnector dbCon, AbstractVideoPlatform abPlat, String identifier)
			throws RateLimitException {

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

		String fullVideoKey = VideoPlatform.getPlatformById(dbCon, platformId).getKey() + "_" + videoIdentifier;

		retObj.put(FULL_VIDEO_KEY, fullVideoKey);

		return retObj;
	}

	private String getFullIdentifier(DatabaseConnector dbCon) {
		VideoPlatform p = VideoPlatform.getPlatformById(dbCon, platformId);

		return p.getKey() + "_" + videoIdentifier;
	}

	public static AbstractOAuth2UserPlatform getOAuth2PlatformIfNeeded(DatabaseConnector dbCon, VideoPlatform plat) {
		if (plat.getOauth2PlatId() > 0) {
			DBOAuth2Platform dBAuth2Plat = DBOAuth2Platform.getPlatformById(dbCon.getRootDbCon(),
					plat.getOauth2PlatId());

			return AbstractOAuth2UserPlatform.getAbstractOAuth2UserPlatformFromType(dBAuth2Plat);
		}
		return null;
	}

	public static PlatformVideo getVideo(DatabaseConnector dbCon, String fullVideoKey) throws RateLimitException {
		String platformKey = fullVideoKey.substring(0, fullVideoKey.indexOf("_"));

		String videoIdent = fullVideoKey.substring(fullVideoKey.indexOf("_") + 1);

		VideoPlatform plat = VideoPlatform.getPlatformByKey(dbCon, platformKey);

		AbstractOAuth2UserPlatform abstractOAuth2UserPlatform = getOAuth2PlatformIfNeeded(dbCon, plat);

		AbstractVideoPlatform abPlat = AbstractVideoPlatform.getPlatformFromConfig(plat, abstractOAuth2UserPlatform);

		return PlatformVideo.getCacheVideo(dbCon, abPlat, videoIdent);
	}

	public String render(DatabaseConnector dbCon) throws RateLimitException {
		return renderBuilder(dbCon).finish();
	}

	public HtmlGenericDiv renderBuilder(DatabaseConnector dbCon) throws RateLimitException {
		VideoPlatform platform = VideoPlatform.getPlatformById(dbCon, platformId);

		AbstractOAuth2UserPlatform abstractOAuth2UserPlatform = getOAuth2PlatformIfNeeded(dbCon, platform);

		AbstractVideoPlatform abPlat = AbstractVideoPlatform.getPlatformFromConfig(platform,
				abstractOAuth2UserPlatform);

		return abPlat.renderVideo(this);
	}

	public static List<PlatformVideo> getLatestVideos(DatabaseConnector dbCon, int platformId, String channelIdentifier)
			throws RateLimitException {

		VideoPlatform p = VideoPlatform.getPlatformById(dbCon, platformId);

		return getLatestVideos(dbCon, p, channelIdentifier);
	}

	public static List<PlatformVideo> getLatestVideos(DatabaseConnector dbCon, VideoPlatform p,
			String channelIdentifier) throws RateLimitException {
		AbstractOAuth2UserPlatform abstractOAuth2UserPlatform = getOAuth2PlatformIfNeeded(dbCon, p);
		return getLatestVideos(dbCon, AbstractVideoPlatform.getPlatformFromConfig(p, abstractOAuth2UserPlatform),
				channelIdentifier);
	}

	public static List<PlatformVideo> getLatestVideos(DatabaseConnector dbCon, AbstractVideoPlatform ap,
			String channelIdentifier) throws RateLimitException {
		return getLatestVideos(dbCon, ap, channelIdentifier, 100);
	}

	public static List<PlatformVideo> getLatestVideos(DatabaseConnector dbCon, AbstractVideoPlatform ap,
			String channelIdentifier, int limit) throws RateLimitException {

		List<PlatformVideo> videos = ap.getLatestVideos(channelIdentifier, limit);

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
				Video v = Video.getVideoByPlatformIdIdentifier(dbCon, ap.getPlatform().getId(),
						pV.getVideoIdentifier());

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

		VideoPlatform p = VideoPlatform.getPlatformById(dbCon, platformId);

		AbstractOAuth2UserPlatform abstractOAuth2UserPlatform = getOAuth2PlatformIfNeeded(dbCon, p);
		AbstractVideoPlatform ap = AbstractVideoPlatform.getPlatformFromConfig(p, abstractOAuth2UserPlatform);

		return ap.getLatestVideos(channelIdentifier, amount);
	}

	public HtmlGenericDiv renderPreview(DatabaseConnector dbCon) throws RateLimitException {
		VideoPlatform p = VideoPlatform.getPlatformById(dbCon, platformId);

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

		AbstractOAuth2UserPlatform abstractOAuth2UserPlatform = getOAuth2PlatformIfNeeded(dbCon, p);
		AbstractVideoPlatform abPlat = AbstractVideoPlatform.getPlatformFromConfig(p, abstractOAuth2UserPlatform);

		channelDiv.writeLink(ViewChannel.LOCATION + "?" + ViewChannel.GET_PARAM_CHANNEL_ID + "=" + p.getKey() + "_"
				+ channelIdentifier, abPlat.getCachedChannelName(channelIdentifier));
		div.write(channelDiv);

		return div;
	}

	public String getOriginalVideoLink(DatabaseConnector dbCon) {
		VideoPlatform p = VideoPlatform.getPlatformById(dbCon, platformId);

		AbstractOAuth2UserPlatform abstractOAuth2UserPlatform = getOAuth2PlatformIfNeeded(dbCon, p);
		AbstractVideoPlatform abPlat = AbstractVideoPlatform.getPlatformFromConfig(p, abstractOAuth2UserPlatform);

		return abPlat.getOriginalVideoLink(this);
	}

}
