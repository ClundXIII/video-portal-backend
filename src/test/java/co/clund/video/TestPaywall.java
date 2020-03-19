package co.clund.video;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;
import co.clund.module.Video;
import co.clund.submodule.video.dbmodel.ExternalSubscription;
import co.clund.submodule.video.dbmodel.Paywall;
import co.clund.submodule.video.dbmodel.PaywallPass;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.util.ResourceUtil;
import co.clund.util.log.LoggingUtil;
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

		JSONObject credentialData = new JSONObject(ResourceUtil.getResourceAsString("/credentials.json")).getJSONObject("video-platform-credentials");

		final DatabaseConnector submoduleConnector = l.getDbCon().getSubmoduleConnector(Video.VIDEO_LOCATION);
		VideoPlatform.populateTestPlatforms(submoduleConnector, credentialData);
		
		VideoPlatform.addNewPlatform(submoduleConnector, "yt01", "Youtube", "youtube", 1);		
		VideoPlatform.addNewPlatform(submoduleConnector, "vi01", "Vimeo", "vimeo", 2);

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
