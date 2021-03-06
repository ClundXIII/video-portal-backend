package co.clund.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;
import co.clund.module.Api;
import co.clund.util.log.LoggingUtil;
import junit.framework.TestCase;

public class ApiTest extends TestCase implements HttpTest {

	public ApiTest(String name) {
		super(name);
	}

	public void testViewApi() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();
		String baseUrl = getIpAndPort(jData);

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		User.addNewLocalUser(l.getDbCon(), "correctUser", "correctPassword", "email@test.com", true);

		startHttpListener(l);

		logger.log(Level.INFO, "testing viewing api");

		assertTrue(httpRequestAsUser("correctUser", "correctPassword", baseUrl, baseUrl + "/" + Api.LOCATION)
				.contains("API Documentation:"));
	}
}