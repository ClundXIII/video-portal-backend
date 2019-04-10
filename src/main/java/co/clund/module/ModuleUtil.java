package co.clund.module;

/*import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;*/

import org.reflections.Reflections;

import co.clund.db.DatabaseConnector;
//import co.clund.db.DatabaseConnector;
import co.clund.util.log.LoggingUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModuleUtil {

	//private static final Map<String, List<String>> moduleFunctionMap = loadModuleFunctionMap("co.clund.module");
	//private static final List<String> functionList = loadFunctionList(moduleFunctionMap);

	private static final Set<Class<? extends AbstractModule>> moduleList = loadModuleClasses();

	private static Set<Class<? extends AbstractModule>> loadModuleClasses() {
		return loadModuleClassesFromPackage("co.clund.module");
	}

    private static Set<Class<? extends AbstractModule>> loadModuleClassesFromPackage(String packagePath) {

		Reflections reflections = new Reflections(packagePath);

		Set<Class<? extends AbstractModule>> retSet = reflections.getSubTypesOf(AbstractModule.class);

		for (Class<? extends AbstractModule> c : retSet) {
			LoggingUtil.createLoggerForModule(c);
		}

		return retSet;
	}

	protected static Set<AbstractModule> createSubModuleClasses(AbstractModule parentModule, String packagePath,
			DatabaseConnector dbCon) {
		if (dbCon == null) {
			throw new RuntimeException("dbCon is null");
		}

		Set<Class<? extends AbstractModule>> classes = loadModuleClassesFromPackage(packagePath);

		Set<AbstractModule> retSet = new HashSet<>();

		Logger logger = LoggingUtil.getDefaultLogger();

		for (Class<? extends AbstractModule> c : classes) {
			if (!Modifier.isAbstract(c.getModifiers())) {
				Constructor<? extends AbstractModule> cons;
				try {
					cons = c.getConstructor(AbstractModule.class, DatabaseConnector.class);
					AbstractModule m = cons.newInstance(parentModule, dbCon);
					logger.log(Level.INFO,
							"Adding module \"" + m.getModuleName() + "\" from class \"" + m.toString() + "\"");
					retSet.add(m);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "error while instanciating module: " + c.getName());
					throw new RuntimeException(e);
				}
			}
		}

		return retSet;
	}

	public static Set<Class<? extends AbstractModule>> getModuleClasses() {
		return moduleList;
	}

	/*private static Map<String, List<String>> loadModuleFunctionMap(String classpath) {
		Set<Class<? extends AbstractModule>> allClasses = new Reflections(classpath)
				.getSubTypesOf(AbstractModule.class);

		Map<String, List<String>> retMap = new HashMap<>();

		for (Class<? extends AbstractModule> c : allClasses) {

			List<String> tmpList = null;
			String name = null;
			try {
				Constructor<? extends AbstractModule> cons = c.getConstructor(String.class, DatabaseConnector.class);
				AbstractModule m = cons.newInstance(new Object[] { "", null });

				tmpList = m.getFunctionList();

				if (tmpList == null) {
					throw new RuntimeException("Module " + m.getModuleName() + " returns null as functionList");
				}

				name = m.getModuleName();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			retMap.put(name, tmpList);
		}

		return retMap;
	}

	private static List<String> loadFunctionList(Map<String, List<String>> tmpModuleFunctionMap) {
		final List<String> retList = new ArrayList<>();
		for (Entry<String, List<String>> s : tmpModuleFunctionMap.entrySet()) {
			String moduleName = s.getKey();
			for (String f : s.getValue()) {
				retList.add(moduleName + "." + f);
			}
		}
		return retList;
	}

	public static Set<String> getModules() {
		return moduleFunctionMap.keySet();
	}

	public static List<String> getFunctionList() {
		return functionList;
	}*/

}
