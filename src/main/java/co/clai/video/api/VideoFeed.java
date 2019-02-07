package co.clai.video.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;

import co.clai.video.UserSession;
import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.ClientCommunityChannelAccess;
import co.clai.video.db.model.ClientCommunityToken;
import co.clai.video.db.model.Video;
import co.clai.video.html.Builder;
import co.clai.video.html.HtmlGenericDiv;
import co.clai.video.module.FunctionResult;
import co.clai.video.platform.PlatformVideo;

public class VideoFeed extends AbstractApiFunction {

	public static String FUNCTION_NAME = "videoFeed";

	public VideoFeed(DatabaseConnector dbCon) {
		super(dbCon, FUNCTION_NAME);
	}

	@Override
	public FunctionResult execute(UserSession s, Map<String, String[]> parameters) {

		String token = parameters.get(GET_PARAM_COMMUNITY_TOKEN)[0];

		ClientCommunityToken tokenC = ClientCommunityToken.getClientCommunityTokenByToken(dbCon, token);

		if (tokenC == null) {
			return new FunctionResult("{\"error\":\"true\"}".getBytes());
		}

		String internalChannels = parameters.get(GET_PARAM_INTERNAL_CHANNEL_IDS)[0];

		List<Integer> channelIds = new ArrayList<>();

		for (String s1 : internalChannels.split(",")) {
			if ("".equals(s1) || s1 == null) {
				continue;
			}

			int channelId = Integer.parseInt(s1);

			ClientCommunityChannelAccess access = ClientCommunityChannelAccess
					.getClientCommunityChannelAccessByCommunityChannel(dbCon, tokenC.getClientCommunityId(), channelId);

			if (access != null) {
				channelIds.add(new Integer(channelId));
			} else {
				logger.log(Level.WARNING, "Community " + tokenC.getClientCommunityId() + " tried to access channel "
						+ channelId + " with token " + token + ": NO ACCESS");
			}
		}

		Map<String, JSONObject> subMap = new HashMap<>();

		for (Integer i : channelIds) {

			List<Video> videos = Video.getLatestVideosByChannelId(dbCon, i.intValue(), 30);
			for (Video v : videos) {
				subMap.put(Video.UPLOAD_DATE_FORMAT.format(v.getDate()), new PlatformVideo(v).toJSONObject(dbCon));
			}
		}

		LinkedList<JSONObject> retList = new LinkedList<>();

		SortedSet<String> keys = new TreeSet<>(subMap.keySet());

		for (String key : keys) {
			retList.addFirst(subMap.get(key));
		}

		JSONArray retArray = new JSONArray();

		for (JSONObject jO : retList) {
			retArray.put(jO);
		}

		return new FunctionResult(retArray.toString(4).getBytes());
	}

	@Override
	public Builder getDocumentation() {

		HtmlGenericDiv docu = new HtmlGenericDiv();

		docu.writeText("Get List of latest videos.\nParameter: ");
		docu.writePre(GET_PARAM_COMMUNITY_TOKEN + "\n   the secure token for your community.\n\n"
				+ GET_PARAM_INTERNAL_CHANNEL_IDS + "\n   comma seperated list of channel ids. Internal only.");

		return docu;
	}

}
