package co.clund.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.video.MainHttpListener;
import co.clund.video.util.ResourceUtil;
import co.clund.video.util.log.LoggingUtil;
import junit.framework.TestCase;

public class StaticTest extends TestCase implements HttpTest {

	public StaticTest(String name) {
		super(name);
	}

	public void testRobots() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		JSONObject conf = getRandomDbAndListeningConfig();

		MainHttpListener l = new MainHttpListener(conf);

		startHttpListener(l);

		assertTrue(httpRequest(getIpAndPort(conf) + "/robots.txt").trim()
				.equals(ResourceUtil.getResourceAsString("/static/robots.txt").trim()));

		l.stop_join();
	}
}
