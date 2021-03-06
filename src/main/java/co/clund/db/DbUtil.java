package co.clund.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reflections.Reflections;

import co.clund.db.model.AbstractDbTable;
import co.clund.util.log.LoggingUtil;

public class DbUtil {

	private final static Logger logger = LoggingUtil.getDefaultLogger();

	public static Set<Class<? extends AbstractDbTable>> getTableClasses(String packagePath) {

		Reflections reflections = new Reflections(packagePath);

		return reflections.getSubTypesOf(AbstractDbTable.class);
	}

	public static List<AbstractDbTable> getCoreTableSet() {

		return getTableSet("co.clund.db.model");
	}

	public static List<AbstractDbTable> getTableSet(String packagePath) {

		Set<Class<? extends AbstractDbTable>> allClasses = getTableClasses(packagePath);

		List<AbstractDbTable> retList = new ArrayList<>();

		for (Class<? extends AbstractDbTable> c : allClasses) {

			try {
				logger.log(Level.INFO, "instanciating " + c.getName());
				retList.add(c.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

		return retList;
	}

	public static void createAllTables(DatabaseConnector dbCon) {
		List<AbstractDbTable> tables = DbUtil.getCoreTableSet();

		for (AbstractDbTable t : tables) {
			logger.log(Level.INFO, "creating table " + t.getTableName());
			AbstractDbTable.createTable(dbCon, t);
		}
	}

	public static void createAllTablesForModule(DatabaseConnector dbCon, String packagePath) {
		List<AbstractDbTable> tables = DbUtil.getTableSet(packagePath);

		for (AbstractDbTable t : tables) {
			logger.log(Level.INFO, "creating table " + t.getTableName());
			AbstractDbTable.createTable(dbCon, t);
		}
	}

}
