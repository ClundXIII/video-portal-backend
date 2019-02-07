package co.clai.video;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clai.video.util.ResourceUtil;
import co.clai.video.util.log.LoggingUtil;
import co.clai.video.MainHttpListener;
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
