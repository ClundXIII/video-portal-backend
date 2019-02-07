package co.clai.video.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clai.video.UserSession;
import co.clai.video.db.DatabaseConnector;
import co.clai.video.html.HtmlPage;

public class ViewChannel extends AbstractModule {

	public static final String LOCATION = "c";

	public ViewChannel(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage p = new HtmlPage("CLAI Video Portal", null, null, s);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		p.writeText("recent uploads here");

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		return allFunctions;
	}
}
