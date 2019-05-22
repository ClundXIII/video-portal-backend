package co.clund.oauth2;

import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import co.clund.db.DatabaseConnector;
import co.clund.db.model.DBOAuth2Platform;

public class VimeoOAuth2Platform extends AbstractOAuth2UserPlatform {

	public VimeoOAuth2Platform(DBOAuth2Platform dBOAuth2Platform) {
		super(dBOAuth2Platform);
	}

	@Override
	public String getPlatformTypeName() {
		return "vimeo";
	}

	@Override
	public long getClientCredentialsExpirationTime() {
		return 30*60*60; // 30 hours; TODO: check what the correct time is!
	}

	@Override
	public URIBuilder getClientCredentialsRequestBuilder(DatabaseConnector dbCon) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URIBuilder getScopelessClientCredentialsRequestBuilder(DatabaseConnector dbCon) {
		// TODO Auto-generated method stub
		return null;
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

}
