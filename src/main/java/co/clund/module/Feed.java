package co.clund.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.html.HtmlPage;

public class Feed extends AbstractModule {

	public static final String FEED_LOCATION = "feed";

	public Feed(@SuppressWarnings("unused") AbstractModule parent, DatabaseConnector dbCon) {
		super("", FEED_LOCATION, dbCon.getSubmoduleConnector(FEED_LOCATION));
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		HtmlPage p = new HtmlPage("Video Portal", null, null, s);

		p.writeH1("Feed");
		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		return new HashMap<>();
	}

}
