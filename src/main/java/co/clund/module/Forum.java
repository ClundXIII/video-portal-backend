package co.clund.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;

public class Forum extends AbstractModule {

	public static final String FORUM_LOCATION = "forum";

	public Forum(@SuppressWarnings("unused") AbstractModule parent, DatabaseConnector dbCon) {
		super("", FORUM_LOCATION, dbCon.getSubmoduleConnector(FORUM_LOCATION));
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		return subModuleMap.get("index").invokePlain(s, parameters);
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		return new HashMap<>();
	}

	@Override
	public Map<String, AbstractModule> loadSubModules() {
		Map<String, AbstractModule> tmpSubModuleMap = new HashMap<>();

		Set<AbstractModule> subModuleSet = ModuleUtil.createSubModuleClasses(this, "co.clund.submodule.forum", dbCon);

		for (AbstractModule m : subModuleSet) {
			tmpSubModuleMap.put(m.getModuleName(), m);
		}

		return tmpSubModuleMap;
	}
}
