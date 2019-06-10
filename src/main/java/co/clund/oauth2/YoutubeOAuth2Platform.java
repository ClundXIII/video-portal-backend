package co.clund.oauth2;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import co.clund.db.DatabaseConnector;
import co.clund.db.model.DBOAuth2Platform;
import co.clund.util.HttpRequestUtil;

public class YoutubeOAuth2Platform extends AbstractOAuth2UserPlatform {

	private static final String OAUTH2_ENTRY = "https://accounts.google.com/o/oauth2/v2/auth";
	private static final String TOKEN_VERIFY_ENTRY = "https://www.googleapis.com/oauth2/v4/token";

	public YoutubeOAuth2Platform(DBOAuth2Platform dBOAuth2Platform) {
		super(dBOAuth2Platform);
	}

	@Override
	public long getClientCredentialsExpirationTime() {
		// it does not expire??
		// https://stackoverflow.com/questions/7030694/why-do-access-tokens-expire
		return -1;
	}

	@Override
	public URIBuilder getClientCredentialsRequestBuilder(DatabaseConnector dbCon) {
		URIBuilder builder = getScopelessClientCredentialsRequestBuilder(dbCon);
		try {
			builder.addParameter("scope", "https://www.googleapis.com/auth/youtube.readonly");
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return builder;
	}

	@Override
	public URIBuilder getScopelessClientCredentialsRequestBuilder(DatabaseConnector dbCon) {
		URIBuilder builder = null;
		try {
			builder = new URIBuilder(OAUTH2_ENTRY);

			builder.addParameter("client_id", getdBOAuth2Platform().getConfig().getString("oauth2_client_id"));
			builder.addParameter("redirect_uri", dbCon.getListener().getSiteUrl() + "/oauth2.callback");
			builder.addParameter("access_type", "offline");
			builder.addParameter("include_granted_scopes", "true");
			builder.addParameter("response_type", "code");

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return builder;
	}

	@Override
	public TokenData getClientCredentialsFromCallback(DatabaseConnector dbCon, Map<String, String[]> callBackData) {
		String code = callBackData.get("code")[0];

		Map<String, String> parameters = new HashMap<>();
		parameters.put("code", code);
		parameters.put("client_id", getdBOAuth2Platform().getConfig().getString("oauth2_client_id"));
		parameters.put("client_secret", getdBOAuth2Platform().getConfig().getString("oauth2_client_secret"));
		parameters.put("redirect_uri", dbCon.getListener().getSiteUrl() + "/oauth2.callback");
		parameters.put("grant_type", "authorization_code");

		try {
			JSONObject refreshData = new JSONObject(
					HttpRequestUtil.httpPostRequestAsString(TOKEN_VERIFY_ENTRY, parameters));

			System.out.println(refreshData.toString(4));

			String accessToken = refreshData.getString("access_token");
			Duration dur = Duration.ofSeconds(refreshData.getInt("expires_in"));
			String refreshToken = refreshData.getString("refresh_token");
			return new TokenData(accessToken, dur, refreshToken);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public String renewClientCredentials(String clientCredentials) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void revokeClientCredentials(String clientCredentials) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPlatformTypeName() {
		return "youtube";
	}
}
