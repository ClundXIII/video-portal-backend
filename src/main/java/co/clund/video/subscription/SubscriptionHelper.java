package co.clund.video.subscription;

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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.ExternalSubscription;
import co.clund.video.db.model.InternalSubscription;
import co.clund.video.db.model.Platform;
import co.clund.video.db.model.User;
import co.clund.video.db.model.Video;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.html.HtmlStyleConstants;
import co.clund.video.platform.PlatformVideo;

public class SubscriptionHelper {

	final DatabaseConnector dbCon;

	public SubscriptionHelper(DatabaseConnector dbCon) {
		this.dbCon = dbCon;
	}

	public List<HtmlGenericDiv> renderOrderedNewestVideosList() {

		List<Video> videos = Video.getLatestVideos(dbCon, 30);

		List<HtmlGenericDiv> retList = new ArrayList<>();

		for (Video v : videos) {
			retList.add(new PlatformVideo(v).renderPreview(dbCon));
		}

		return retList;
	}

	public List<HtmlGenericDiv> renderOrderedSubscribedVideosFromList(List<String> channelKeys) throws Exception {

		Map<String, HtmlGenericDiv> subMap = new HashMap<>();

		List<Future<List<PlatformVideo>>> videosFutureList = new ArrayList<>();

		for (String channelKey : channelKeys) {

			String platformKey = channelKey.substring(0, channelKey.indexOf("_"));
			String channelIdentifier = channelKey.substring(channelKey.indexOf("_") + 1);

			Platform plat = Platform.getPlatformByKey(dbCon, platformKey);

			FutureTask<List<PlatformVideo>> futureTask = new FutureTask<>(new Callable<List<PlatformVideo>>() {
				@Override
				public List<PlatformVideo> call() {
					return PlatformVideo.getLatestVideos(dbCon, plat, channelIdentifier);
				}
			});

			@SuppressWarnings("unchecked")
			FutureTask<List<PlatformVideo>> future = (FutureTask<List<PlatformVideo>>) dbCon
					.getThreadExecutorMap(plat.getId()).insertTask("videosFromChannel" + channelIdentifier, futureTask);

			videosFutureList.add(future);
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

		for (ExternalSubscription exSub : ExternalSubscription.getExternalSubscriptionByUserId(dbCon,
				thisUser.getId())) {

			FutureTask<List<PlatformVideo>> futureTask = new FutureTask<>(new Callable<List<PlatformVideo>>() {
				@Override
				public List<PlatformVideo> call() {
					return PlatformVideo.getLatestVideos(dbCon, exSub.getPlatformId(), exSub.getChannelIdentifier());
				}
			});

			@SuppressWarnings("unchecked")
			FutureTask<List<PlatformVideo>> future = (FutureTask<List<PlatformVideo>>) dbCon
					.getThreadExecutorMap(exSub.getPlatformId())
					.insertTask("videosFromChannel" + exSub.getChannelIdentifier(), futureTask);

			videosFutureList.add(future);
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

			retList.add(platformVideo.renderPreview(dbCon));
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

			retList.add(platformVideo.renderPreview(dbCon));
		}

		return retList;
	}

}
