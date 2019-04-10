package co.clund.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;
import co.clund.exception.RateLimitException;
import co.clund.html.HtmlGenericDiv;
import co.clund.html.HtmlStyleConstants;
import co.clund.submodule.video.dbmodel.ExternalSubscription;
import co.clund.submodule.video.dbmodel.InternalSubscription;
import co.clund.submodule.video.dbmodel.Platform;
import co.clund.submodule.video.dbmodel.Video;
import co.clund.submodule.video.platform.PlatformVideo;
import co.clund.util.cache.DynamicAsyncExpiringCache;

public class SubscriptionHelper {

	private static final String TASK_NAME_VIDEOS_FROM_CHANNEL = "videosFromChannel";

	public static final DynamicAsyncExpiringCache<List<PlatformVideo>> videoCache = new DynamicAsyncExpiringCache<>(
			TASK_NAME_VIDEOS_FROM_CHANNEL, 3 * 60); // 3 Minutes

	final DatabaseConnector dbCon;

	private final co.clund.module.Video videoModule;

	public SubscriptionHelper(DatabaseConnector dbCon, co.clund.module.Video videoModule) {
		this.dbCon = dbCon;

		this.videoModule = videoModule;//(co.clund.module.Video) dbCon.getListener().getReqHandler().moduleMap.get(co.clund.module.Video.VIDEO_LOCATION);
	}

	public List<HtmlGenericDiv> renderOrderedNewestVideosList() {

		List<Video> videos = Video.getLatestVideos(dbCon, 30);

		List<HtmlGenericDiv> retList = new ArrayList<>();

		for (Video v : videos) {
			try {
				retList.add(new PlatformVideo(v).renderPreview(dbCon));
			} catch (RateLimitException e) {
				e.printStackTrace();
			}
		}

		return retList;
	}

	public List<HtmlGenericDiv> renderOrderedSubscribedVideosFromList(List<String> channelKeys) throws Exception {
		List<Future<List<PlatformVideo>>> videosFutureList = new ArrayList<>();

		List<List<PlatformVideo>> cachedVideosList = new ArrayList<>();

		for (String channelKey : channelKeys) {

			List<PlatformVideo> tmpList = videoCache.retrieve(channelKey);
			if (tmpList != null) {
				cachedVideosList.add(tmpList);
				continue;
			}

			String platformKey = channelKey.substring(0, channelKey.indexOf("_"));
			String channelIdentifier = channelKey.substring(channelKey.indexOf("_") + 1);

			Platform plat = Platform.getPlatformByKey(dbCon, platformKey);

			this.setupFutureTask(channelIdentifier, plat, videosFutureList);
		}

		return assembleOrderedVideoDivList(new HashMap<>(), videosFutureList, cachedVideosList);
	}

	public List<HtmlGenericDiv> renderOrderedSubscribedVideosList(User thisUser) throws Exception {

		Map<String, HtmlGenericDiv> subMap = new HashMap<>();

		for (InternalSubscription intSub : InternalSubscription.getInternalSubscriptionByUserId(dbCon,
				thisUser.getId())) {
			List<Video> videos = Video.getLatestVideosByChannelId(dbCon, intSub.getChannelId(), 30);

			for (Video v : videos) {

				PlatformVideo platVid = new PlatformVideo(v);

				subMap.put(Video.UPLOAD_DATE_FORMAT.format(v.getDate()), platVid.renderPreview(dbCon));
			}
		}

		List<Future<List<PlatformVideo>>> videosFutureList = new ArrayList<>();

		List<List<PlatformVideo>> cachedVideosList = new ArrayList<>();

		for (ExternalSubscription exSub : ExternalSubscription.getExternalSubscriptionByUserId(dbCon,
				thisUser.getId())) {

			String channelKey = Platform.getPlatformById(dbCon, exSub.getPlatformId()).getKey() + "_"
					+ exSub.getChannelIdentifier();
			List<PlatformVideo> tmpList = videoCache.retrieve(channelKey);
			if (tmpList != null) {
				cachedVideosList.add(tmpList);
				continue;
			}

			String channelIdentifier = exSub.getChannelIdentifier();

			Platform plat = Platform.getPlatformById(dbCon, exSub.getPlatformId());

			setupFutureTask(channelIdentifier, plat, videosFutureList);
		}

		return assembleOrderedVideoDivList(subMap, videosFutureList, cachedVideosList);
	}

	private void setupFutureTask(String channelIdentifier, Platform plat,
			List<Future<List<PlatformVideo>>> videosFutureList) {
		FutureTask<List<PlatformVideo>> futureTask = new FutureTask<>(new Callable<List<PlatformVideo>>() {
			@Override
			public List<PlatformVideo> call() {
				try {
					List<PlatformVideo> videoList = PlatformVideo.getLatestVideos(dbCon, plat.getId(), channelIdentifier);

					videoCache.put(plat.getKey() + "_" + channelIdentifier, videoList);

					return videoList;
				} catch (RateLimitException e) {
					e.printStackTrace();
					return new ArrayList<>();
				}
			}
		});

		@SuppressWarnings("unchecked")
		FutureTask<List<PlatformVideo>> future = (FutureTask<List<PlatformVideo>>) videoModule.getThreadExecutorMap(plat.getId())
				.insertTask(TASK_NAME_VIDEOS_FROM_CHANNEL + channelIdentifier, futureTask);

		videosFutureList.add(future);
	}

	private LinkedList<HtmlGenericDiv> assembleOrderedVideoDivList(Map<String, HtmlGenericDiv> subMap,
			List<Future<List<PlatformVideo>>> videosFutureList, List<List<PlatformVideo>> cachedVideosList)
			throws RateLimitException, InterruptedException, ExecutionException {
		for (List<PlatformVideo> videos : cachedVideosList) {
			for (PlatformVideo v : videos) {
				subMap.put(Video.UPLOAD_DATE_FORMAT.format(v.getDate()), v.renderPreview(dbCon));

				Video dbVideo = Video.getVideoByPlatformIdIdentifier(dbCon, v.getPlatformId(), v.getVideoIdentifier());
				if (dbVideo == null) {
					Video.addNewVideo(dbCon, -1, -1, v.getChannelIdentifier(), v.getTitle(), v.getPlatformId(),
							v.getVideoIdentifier(), Video.UPLOAD_DATE_FORMAT.format(v.getDate()), v.getDescription(),
							v.getThumbnailLink(), true);
				}
			}
		}

		for (Future<List<PlatformVideo>> f : videosFutureList) {

			List<PlatformVideo> videos = f.get();

			for (PlatformVideo v : videos) {
				subMap.put(Video.UPLOAD_DATE_FORMAT.format(v.getDate()), v.renderPreview(dbCon));

				Video dbVideo = Video.getVideoByPlatformIdIdentifier(dbCon, v.getPlatformId(), v.getVideoIdentifier());
				if (dbVideo == null) {
					Video.addNewVideo(dbCon, -1, -1, v.getChannelIdentifier(), v.getTitle(), v.getPlatformId(),
							v.getVideoIdentifier(), Video.UPLOAD_DATE_FORMAT.format(v.getDate()), v.getDescription(),
							v.getThumbnailLink(), true);
				}
			}
		}

		LinkedList<HtmlGenericDiv> retList = new LinkedList<>();

		SortedSet<String> keys = new TreeSet<>(subMap.keySet());

		for (String key : keys) {
			retList.addFirst(subMap.get(key));
		}
		return retList;
	}

	public List<HtmlGenericDiv> renderLiveStreams(User thisUser) {
		// TODO Auto-generated method stub

		List<HtmlGenericDiv> retList = new ArrayList<>();

		for (int i = 0; i < 7; i++) {
			HtmlGenericDiv div = new HtmlGenericDiv("", HtmlStyleConstants.DIV_CLASS_VIDEO_PREVIEW);
			div.writeText("Livestream #" + i);
			retList.add(div);
		}

		return retList;
	}

	public List<HtmlGenericDiv> renderSuggestedVideos(PlatformVideo vid, int count) {

		List<HtmlGenericDiv> retList = new ArrayList<>();

		List<Video> latestChannelVideos = Video.getLatestVideosByPlatformChannelIdentifier(dbCon, vid.getPlatformId(),
				vid.getChannelIdentifier(), count * 10);

		Set<String> videoIdentifier = new HashSet<>();

		while ((retList.size() < (count / 2)) && (latestChannelVideos.size() > 0)) {
			int randomVideoPosition = (int) Math.random() * latestChannelVideos.size();

			final PlatformVideo platformVideo = new PlatformVideo(latestChannelVideos.remove(randomVideoPosition));

			videoIdentifier.add(platformVideo.getVideoIdentifier());

			try {
				retList.add(platformVideo.renderPreview(dbCon));
			} catch (RateLimitException e) {
				e.printStackTrace();
			}
		}

		List<Video> latestVideos = Video.getLatestVideos(dbCon, count * 10);

		while ((retList.size() < count) && (latestVideos.size() > 0)) {
			int randomVideoPosition = (int) Math.random() * latestVideos.size();

			final PlatformVideo platformVideo = new PlatformVideo(latestVideos.remove(randomVideoPosition));

			if (videoIdentifier.contains(platformVideo.getVideoIdentifier())) {
				continue;
			}

			if (platformVideo.getChannelIdentifier() == vid.getChannelIdentifier()) {
				continue;
			}

			try {
				retList.add(platformVideo.renderPreview(dbCon));
			} catch (RateLimitException e) {
				e.printStackTrace();
			}
		}

		return retList;
	}

}
