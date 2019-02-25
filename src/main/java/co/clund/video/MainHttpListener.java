package co.clund.video;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import org.json.*;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.util.cache.ExpiringCache;
import co.clund.video.util.log.Jetty2LoggerBridge;
import co.clund.video.util.log.LoggingUtil;

public class MainHttpListener {

	private static final String JSON_VARNAME_SESSION_KEEP_ALIVE = "sessionKeepAlive";
	public static final String JSON_VARNAME_IP = "ip";
	public static final String JSON_VARNAME_PORTS = "ports";
	public static final String JSON_VARNAME_LISTEN = "listen";
	public static final String JSON_VARNAME_DB = "db";

	public static final String JSON_VARNAME_SITE_URL = "siteUrl";

	Logger logger = LoggingUtil.getDefaultLogger();

	private class ListenInfo {
		private final String ip;
		private final int[] ports;

		public ListenInfo(String ip, int[] ports) {
			this.ip = ip;
			this.ports = ports;
		}

		public void setupServer(Server server) {
			for (int port : ports) {
				@SuppressWarnings("resource")
				ServerConnector connector = new ServerConnector(server, 1, 1);
				connector.setHost(ip);
				connector.setPort(port);
				server.addConnector(connector);
			}
		}
	}

	private List<ListenInfo> lInfo = new ArrayList<>();

	private final RequestHandler reqHandler;
	
	public JSONObject dbConfig;

	public RequestHandler getReqHandler() {
		return reqHandler;
	}

	private final Server server;
	private final DatabaseConnector dbCon;

	private final String siteUrl;

	public DatabaseConnector getDbCon() {
		return dbCon;
	}

	public MainHttpListener(JSONObject config) {
		LoggingUtil.setup();

		Jetty2LoggerBridge tmpL = new Jetty2LoggerBridge("jetty web server");
		tmpL.setDebugEnabled(false);

		System.setProperty("org.eclipse.jetty.LEVEL", "WARN");

		org.eclipse.jetty.util.log.Log.setLog(tmpL);

		java.util.Locale.setDefault(java.util.Locale.ENGLISH);

		dbConfig = config.getJSONObject(JSON_VARNAME_DB);

		dbCon = new DatabaseConnector(this, dbConfig);

		reqHandler = new RequestHandler(this.dbCon);

		server = new Server(new QueuedThreadPool(1024, 1));

		siteUrl = config.getString(JSON_VARNAME_SITE_URL);

		JSONArray jsonListeners = config.getJSONArray(JSON_VARNAME_LISTEN);

		for (int i = 0; i < jsonListeners.length(); i++) {
			JSONObject jListener = jsonListeners.getJSONObject(i);

			String ip = jListener.getString(JSON_VARNAME_IP);

			int ports[] = new int[128];

			JSONArray jPorts = jListener.getJSONArray(JSON_VARNAME_PORTS);

			for (int j = 0; j < jPorts.length(); j++) {
				ports[j] = jPorts.getInt(j);
				logger.log(Level.INFO, "Initializing Listener at " + ip + ":" + ports[j]);
				lInfo.add(new ListenInfo(ip, ports));
			}

		}

		for (ListenInfo l : lInfo) {
			l.setupServer(server);
		}

		int sessionKeepAlive = Integer.parseInt(config.getString(JSON_VARNAME_SESSION_KEEP_ALIVE));

		SessionHandler s = new SessionHandler();
		s.setMaxInactiveInterval(60 * 60 * sessionKeepAlive);
		s.setHandler(reqHandler);

		ContextHandler c = new ContextHandler("/");
		c.setContextPath("/");
		c.setHandler(s);

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { c });
		server.setHandler(contexts);

	}

	public void run() {
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void stop_join() {

		try {
			server.stop();

			server.join();

			ExpiringCache.stop();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String getSiteUrl() {
		return siteUrl;
	}

}
