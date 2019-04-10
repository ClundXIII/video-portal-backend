package co.clund.video;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.db.DatabaseConnector;
import co.clund.module.AbstractModule;
import co.clund.module.ModuleUtil;
import co.clund.module.Video;
import co.clund.util.log.LoggingUtil;
import junit.framework.TestCase;

public class ModuleUtilTest extends TestCase implements HttpTest {

	public ModuleUtilTest(String name) {
		super(name);
	}

	public void testAllFunctionsAndModules() {
		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "running test " + getName());

		logger.log(Level.INFO, "getting Module and Function List:");

		Set<Class<? extends AbstractModule>> allClasses = ModuleUtil.getModuleClasses();

		JSONObject jData = getRandomDbAndListeningConfig();

		MainHttpListener l = new MainHttpListener(jData);

		DatabaseConnector.initializeDatabase(l.getDbCon());

		printSuperModulePath(logger, allClasses, l.getDbCon().getSubmoduleConnector(Video.VIDEO_LOCATION));
	}

	private static void printSuperModulePath(Logger logger, Set<Class<? extends AbstractModule>> allClasses, DatabaseConnector dbCon) {
		for (Class<? extends AbstractModule> c : allClasses) {
			if (!Modifier.isAbstract(c.getModifiers())) {
				Constructor<? extends AbstractModule> cons;
				try {
					cons = c.getConstructor(AbstractModule.class, DatabaseConnector.class);
					AbstractModule m = cons.newInstance(null, dbCon);
					logger.log(Level.INFO, "Module: " + m.getModulePath());
					for (String f : m.getFunctionList()) {
						logger.log(Level.INFO, "Function: " + m.getModulePath() + "." + f);
					}
					printModulePath(logger, m.loadSubModules());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private static void printModulePath(Logger logger, Map<String, AbstractModule> subModules) {
		for (Entry<String, AbstractModule> entry : subModules.entrySet()) {
			try {
				AbstractModule m = entry.getValue();
				logger.log(Level.INFO, "Module: " + m.getModulePath());
				for (String f : m.getFunctionList()) {
					logger.log(Level.INFO, "Function: " + m.getModulePath() + "." + f);
				}
				printModulePath(logger, m.loadSubModules());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
