package co.clund.util.log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import co.clund.module.AbstractModule;

public class LoggingUtil {

	public final static String LOGGING_DESTINATION_DIR = "log/";

	private static final Map<String, Logger> moduleLoggerList = new HashMap<>();

	public static Logger getDefaultLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

	private static boolean isSetup = false;

	public static void setup() {

		if (isSetup) {
			return;
		}

		isSetup = true;

		createLogDir();

		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.FINE);
		logger.setUseParentHandlers(false);

		FileHandler fileTxt;
		try {
			fileTxt = new FileHandler(LOGGING_DESTINATION_DIR + "general.log", true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// create a TXT formatter
		Formatter formatterTxt = new GeneralOutputFormatter();
		fileTxt.setFormatter(formatterTxt);
		logger.addHandler(fileTxt);

		ConsoleHandler hCon = new ConsoleHandler();
		hCon.setFormatter(new GeneralOutputFormatter());
		logger.addHandler(hCon);

		// create api logger:

		Logger l = Logger.getLogger("api", null);
		l.setLevel(Level.FINE);
		l.setUseParentHandlers(false);

		l.setLevel(Level.ALL);
		FileHandler fileHandler;
		try {
			fileHandler = new FileHandler(LOGGING_DESTINATION_DIR + "api.log", true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// create a TXT formatter
		ModuleOutputFormatter formatter = new ModuleOutputFormatter();
		fileHandler.setFormatter(formatter);
		l.addHandler(fileHandler);

		ConsoleHandler hCon1 = new ConsoleHandler();
		hCon1.setFormatter(new GeneralOutputFormatter());
		l.addHandler(hCon1);
	}

	public static void createLoggerForModule(Class<? extends AbstractModule> c) {

		createLogDir();

		try {

			Logger l = Logger.getLogger("module-" + c.getName(), null);
			l.setLevel(Level.FINE);
			l.setUseParentHandlers(false);

			moduleLoggerList.put(c.getName(), l);

			l.setLevel(Level.ALL);
			FileHandler fileHandler = new FileHandler(LOGGING_DESTINATION_DIR + c.getSimpleName() + ".log", true);

			// create a TXT formatter
			ModuleOutputFormatter formatter = new ModuleOutputFormatter();
			fileHandler.setFormatter(formatter);
			l.addHandler(fileHandler);

			ConsoleHandler hCon = new ConsoleHandler();
			hCon.setFormatter(new GeneralOutputFormatter());
			l.addHandler(hCon);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private static void createLogDir() {
		File f = new File(LOGGING_DESTINATION_DIR);

		if (f.isFile()) {
			throw new RuntimeException(LOGGING_DESTINATION_DIR + " should be a directory and not a file!");
		}

		if (!f.isDirectory()) {
			f.mkdir();
		}
	}

	public static Logger getLoggerFromModule(Class<? extends AbstractModule> c) {
		return Logger.getLogger("module-" + c.getName(), null);
	}

	public static Logger getApiLogger() {
		setup();

		return Logger.getLogger("api", null);
	}
}
