package co.clund.video.api;

import java.util.Map;
import java.util.logging.Level;

import co.clund.video.UserSession;
import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.ClientCommunityChannelAccess;
import co.clund.video.db.model.ClientCommunityToken;
import co.clund.video.exception.RateLimitException;
import co.clund.video.html.Builder;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.module.FunctionResult;
import co.clund.video.platform.PlatformVideo;

public class Video extends AbstractApiFunction {

	public static String FUNCTION_NAME = "video";

	public Video(DatabaseConnector dbCon) {
		super(dbCon, FUNCTION_NAME);
	}

	@Override
	public FunctionResult execute(UserSession s, Map<String, String[]> parameters) {

		String token = parameters.get(GET_PARAM_COMMUNITY_TOKEN)[0];

		ClientCommunityToken tokenC = ClientCommunityToken.getClientCommunityTokenByToken(dbCon, token);

		if (tokenC == null) {
			return new FunctionResult("{\"error\":\"true\"}".getBytes());
		}

		PlatformVideo vid;
		try {
			vid = PlatformVideo.getVideo(dbCon, parameters.get(PlatformVideo.FULL_VIDEO_KEY)[0]);
		} catch (RateLimitException e) {
			logger.log(Level.SEVERE, e.getMessage());
			return new FunctionResult("{\"error\":\"true\"}".getBytes());
		}

		ClientCommunityChannelAccess access = ClientCommunityChannelAccess
				.getClientCommunityChannelAccessByCommunityChannel(dbCon, tokenC.getClientCommunityId(),
						vid.getChannelId());

		if (access == null) {
			logger.log(Level.WARNING, "Community " + tokenC.getClientCommunityId() + " tried to access channel "
					+ vid.getChannelId() + " with token " + token + ": NO ACCESS");
			return new FunctionResult("{\"error\":\"NO_ACCESS\"}".getBytes());
		}

		return new FunctionResult(vid.toJSONObject(dbCon).toString(4).getBytes());
	}

	@Override
	public Builder getDocumentation() {

		HtmlGenericDiv docu = new HtmlGenericDiv();

		docu.writeText("Information about a video.\nParameter: ");
		docu.writePre(GET_PARAM_COMMUNITY_TOKEN + "\n   the secure token for your community.\n\n"
				+ PlatformVideo.FULL_VIDEO_KEY + "\n   the full video key.\n\n");

		return docu;
	}

}
