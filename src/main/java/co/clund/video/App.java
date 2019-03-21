package co.clund.video;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.User;
import co.clund.video.util.FileUtil;
import co.clund.video.util.ResourceUtil;
import co.clund.video.util.log.LoggingUtil;

/**
 * class for with main function
 */
public class App {

	private static final String RESOURCE_LOCATION_CONFIG_JSON = "config.json";
	private static final byte[] DEFAULT_CONFIG_CONTENT = ResourceUtil
			.getResourceAsByteArr("/" + RESOURCE_LOCATION_CONFIG_JSON);

	public static void main(String[] args) {

		LoggingUtil.setup();

		Logger logger = LoggingUtil.getDefaultLogger();

		logger.log(Level.INFO, "starting program ...");

		if (args.length == 0) {
			System.out.println("Welcome to the Video Portal Backend!");
			System.out.println("usage:");
			System.out.println("java -jar video-portal-backend.jar generate-tables <config file>");
			System.out.println("  --> generates the initial tables");
			System.out.println("");
			System.out.println("java -jar video-portal-backend.jar add-superadmin <config file> <Username> <Password> <email>");
			System.out.println("  --> adds a user with root flag");
			System.out.println("");
			System.out.println("java -jar video-portal-backend.jar <config file>");
			System.out.println("  --> start the video platform");
			System.out.println("");

			File f = new File(RESOURCE_LOCATION_CONFIG_JSON);
			if (f.isFile()) {
				System.out.println("exiting ...");
				System.exit(0);
			}

			System.out.println("Writing default config file \"config.json\" ...");

			try {
				f.createNewFile();
				Files.write(f.toPath(), DEFAULT_CONFIG_CONTENT, StandardOpenOption.CREATE_NEW);
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("done!");
			System.exit(0);
		}

		if (args.length == 1) {

			JSONObject config = new JSONObject(FileUtil.getFileContentAsString(args[0]));

			MainHttpListener l = new MainHttpListener(config);

			l.run();

			try {
				System.in.read();
			} catch (IOException e) {
				logger.log(Level.INFO, e.getMessage());
			}

			l.stop_join();

			logger.log(Level.INFO, "stopped!");
		} else {

			switch (args[0]) {
			case "generate-tables": {
				JSONObject config = new JSONObject(FileUtil.getFileContentAsString(args[1]));

				DatabaseConnector dbCon = new DatabaseConnector(null,
						config.getJSONObject(MainHttpListener.JSON_VARNAME_DB));

				DatabaseConnector.initializeDatabase(dbCon);
				break;
			}

			case "add-superadmin": {
				JSONObject config = new JSONObject(FileUtil.getFileContentAsString(args[1]));

				DatabaseConnector dbCon = new DatabaseConnector(null,
						config.getJSONObject(MainHttpListener.JSON_VARNAME_DB));

				User.addNewLocalUser(dbCon, args[2], args[3], args[4], true);

				break;
			}

			default:
				logger.log(Level.INFO, "unknown command");
				System.exit(1);
			}

			logger.log(Level.INFO, "done!");
		}

		System.exit(0);
	}
}
