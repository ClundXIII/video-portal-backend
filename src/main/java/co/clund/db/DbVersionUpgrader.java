package co.clund.db;

import java.util.logging.Level;

import co.clund.submodule.core.dbmodel.TConfiguration;
import co.clund.util.log.LoggingUtil;

public class DbVersionUpgrader {

	public static final long CURRENT_DB_VERSION_NUMBER = 1;

	public static void checkForUpgrades(DatabaseConnector dbCon) {

		switch (TConfiguration.getDbVersion(dbCon.getSubmoduleConnector("core"))) {
		case 1:
			// nothing to do

			break;

		default:
			/// OH SHIT
			LoggingUtil.getDefaultLogger().log(Level.SEVERE,
					"cannot determine Database version number, maybe the database is from a newer version?");
			throw new RuntimeException("Database is too new!");
		}

	}

}
