package co.clai.video;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.ExternalSubscription;
import co.clai.video.db.model.Paywall;
import co.clai.video.db.model.PaywallPass;
import co.clai.video.db.model.Platform;
import co.clai.video.db.model.User;
import co.clai.video.util.ResourceUtil;
import co.clai.video.util.log.LoggingUtil;
import junit.framework.TestCase;

public class TestPaywall extends TestCase implements HttpTest {

	public TestPaywall(String name) {
		super(name);
	}

	public void testExtending() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject jData = getRandomDbAndListeningConfig();

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json")).getJSONObject("credentials");

		Platform.populateTestPlatforms(l.getDbCon(), credentialData);

		User.addNewLocalUser(l.getDbCon(), "testUser1", "asdf1234", "test@clai.co", false);

		ExternalSubscription.addNewExternalSubscription(l.getDbCon(), 1, 1, "UCJs1mfRk0orBF9twGXaZA2w");

		Paywall.addNewPaywall(l.getDbCon(), 1, "Test Paywall", "test_paywall", new JSONObject());

		PaywallPass.extendPaywallPass(l.getDbCon(), 1, 1, Period.ZERO.plusDays(30));

		PaywallPass pwP = PaywallPass.getPaywallPassByUserIdPaywallId(l.getDbCon(), 1, 1);

		assertTrue(pwP.getExpire().isAfter(LocalDateTime.now().plusDays(29)));
		assertTrue(pwP.getExpire().isBefore(LocalDateTime.now().plusDays(31)));

		PaywallPass.extendPaywallPass(l.getDbCon(), 1, 1, Period.ZERO.plusDays(20));

		pwP = PaywallPass.getPaywallPassByUserIdPaywallId(l.getDbCon(), 1, 1);

		assertTrue(pwP.getExpire().isAfter(LocalDateTime.now().plusDays(49)));
		assertTrue(pwP.getExpire().isBefore(LocalDateTime.now().plusDays(51)));
	}

}
