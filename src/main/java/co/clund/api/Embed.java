package co.clund.api;

import java.util.Map;
import java.util.logging.Level;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.exception.RateLimitException;
import co.clund.html.Builder;
import co.clund.html.HtmlGenericDiv;
import co.clund.module.FunctionResult;
import co.clund.submodule.video.dbmodel.ClientCommunityChannelAccess;
import co.clund.submodule.video.dbmodel.ClientCommunityToken;
import co.clund.submodule.video.platform.PlatformVideo;

public class Embed extends AbstractApiFunction {

	public static String FUNCTION_NAME = "embed";

	public Embed(DatabaseConnector dbCon) {
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

		try {
			return new FunctionResult(vid.render(dbCon).getBytes());
		} catch (RateLimitException e) {
			logger.log(Level.SEVERE, e.getMessage());
			return new FunctionResult("{\"error\":\"true\"}".getBytes());
		}
	}

	@Override
	public Builder getDocumentation() {

		HtmlGenericDiv docu = new HtmlGenericDiv();

		docu.writeText("Get Html code for embedding a video.\nParameter: ");
		docu.writePre(GET_PARAM_COMMUNITY_TOKEN + "\n   the secure token for your community.\n\n"
				+ PlatformVideo.FULL_VIDEO_KEY + "\n   the full video key.\n\n");

		return docu;
	}

}
