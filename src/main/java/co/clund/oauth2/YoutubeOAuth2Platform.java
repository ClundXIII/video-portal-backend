package co.clund.oauth2;

import java.util.Map;
import java.util.logging.Level;

import org.apache.http.client.utils.URIBuilder;

import co.clund.db.DatabaseConnector;
import co.clund.db.model.DBOAuth2Platform;

public class YoutubeOAuth2Platform extends AbstractOAuth2UserPlatform {

	private static final String OAUTH2_ENTRY = "https://accounts.google.com/o/oauth2/v2/auth";

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

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return builder;
	}

	@Override
	public String getClientCredentialsFromCallback(Map<String, String> callBackData) {
		// TODO Auto-generated method stub
		return null;
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
