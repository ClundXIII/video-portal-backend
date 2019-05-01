package co.clund.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.User;
import co.clund.html.HtmlPage;
import co.clund.module.AbstractModule;
import co.clund.module.FunctionResult;
import co.clund.module.Profile;

public class Index extends AbstractModule {

	public static final String INDEX_LOCATION = "index";

	public Index(@SuppressWarnings("unused") AbstractModule parent, DatabaseConnector dbCon) {
		super("", INDEX_LOCATION, dbCon);

	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage p = new HtmlPage("<REPLACE with website name>", s, dbCon, "/" + INDEX_LOCATION);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		User thisUser = s.getThisUser();
		if (thisUser == null) {
			p.writeLink(Profile.LOCATION, "Log in");
		}

		p.writeH1("News");

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		return allFunctions;
	}
}
