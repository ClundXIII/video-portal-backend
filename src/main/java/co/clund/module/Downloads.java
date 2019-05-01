package co.clund.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.html.HtmlPage;

public class Downloads extends AbstractModule {

	public static String DOWNLOADS_LOCATION = "downloads";
	
	public Downloads(@SuppressWarnings("unused") AbstractModule parent, DatabaseConnector dbCon) {
		super("", DOWNLOADS_LOCATION, dbCon.getSubmoduleConnector(DOWNLOADS_LOCATION));
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		HtmlPage p = new HtmlPage("Video Portal", s, dbCon, "/" + DOWNLOADS_LOCATION);

		p.writeH1("Content");
		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		return new HashMap<>();
	}

}
