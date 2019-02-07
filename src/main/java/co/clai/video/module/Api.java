package co.clai.video.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import co.clai.video.UserSession;
import co.clai.video.api.AbstractApiFunction;
import co.clai.video.db.DatabaseConnector;
import co.clai.video.html.HtmlPage;

public class Api extends AbstractModule {

	public static final String LOCATION = "api";

	/**
	 * Workaround. Due to java class structure these are other API function as the
	 * ones being use when invoking. No problem since documentation is static
	 * content. Might need a complete module redesign to properly implement this.
	 */
	private final Map<String, AbstractApiFunction> documentationOnlyApiFunctions = loadApiFunctions();

	public Api(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		HtmlPage p = new HtmlPage("Api Documentation", null, null, s);

		if ((s.getThisUser() == null) || (!s.getThisUser().getIsRoot())) {
			p.writeText("no access");
			return p.finish().getBytes();
		}

		p.writeH1("API Documentation:");

		for (Entry<String, AbstractApiFunction> e : documentationOnlyApiFunctions.entrySet()) {

			AbstractApiFunction func = e.getValue();

			p.writeHline();

			p.writeH3("Function \"" + func.getFunctionName() + "\":");
			p.write(func.getDocumentation());
		}

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		final Map<String, AbstractApiFunction> tmpApiFunctions = new HashMap<>();

		for (AbstractApiFunction f : AbstractApiFunction.getAllFunctions(dbCon)) {
			tmpApiFunctions.put(f.getFunctionName(), f);
		}

		for (Entry<String, AbstractApiFunction> e : tmpApiFunctions.entrySet()) {
			allFunctions.put(e.getKey(), e.getValue()::execute);
		}

		return allFunctions;
	}

	private Map<String, AbstractApiFunction> loadApiFunctions() {
		final Map<String, AbstractApiFunction> tmpApiFunctions = new HashMap<>();

		for (AbstractApiFunction f : AbstractApiFunction.getAllFunctions(dbCon)) {
			tmpApiFunctions.put(f.getFunctionName(), f);
		}

		return tmpApiFunctions;
	}

}
