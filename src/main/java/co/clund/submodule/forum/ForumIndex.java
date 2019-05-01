package co.clund.submodule.forum;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.html.HtmlPage;
import co.clund.module.AbstractModule;
import co.clund.module.FunctionResult;

public class ForumIndex extends AbstractModule {

	public static final String INDEX_LOCATION = "index";

	public ForumIndex(AbstractModule parent, DatabaseConnector dbCon) {
		super(parent.getModulePath(), INDEX_LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		HtmlPage p = new HtmlPage("Video Portal", s, dbCon, "/forum/" + INDEX_LOCATION);

		p.writeH1("Forum");
		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		return new HashMap<>();
	}

}
