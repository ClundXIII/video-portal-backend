package co.clund.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.logging.Level;

import org.json.JSONObject;

import co.clund.MainHttpListener;
import co.clund.module.AbstractModule;
import co.clund.module.Core;
import co.clund.submodule.core.dbmodel.TBlock;
import co.clund.submodule.core.dbmodel.TBlockRegionRelation;
import co.clund.submodule.core.dbmodel.TConfiguration;
import co.clund.submodule.core.dbmodel.TMenuStructure;
import co.clund.submodule.core.dbmodel.TSiteTemplate;
import co.clund.util.log.LoggingUtil;

public class DatabaseConnector {

	private final String dbPath;
	private final String dbUser;
	private final String dbPassword;
	public final String dbPrefix;

	private final MainHttpListener listener;

	private final java.util.logging.Logger logger = LoggingUtil.getDefaultLogger();

	public final String uniqueKey;
	
	private final DatabaseConnector rootDbCon;

	public DatabaseConnector getRootDbCon() {
		return rootDbCon;
	}

	public DatabaseConnector(MainHttpListener listener, JSONObject dbConfig) {
		dbPath = dbConfig.getString("path");
		dbUser = dbConfig.getString("username");
		dbPassword = dbConfig.getString("password");

		if (dbConfig.has("dbPrefix")) {
			dbPrefix = dbConfig.getString("dbPrefix") + "_";
		} else {
			dbPrefix = "";
		}

		this.listener = listener;

		uniqueKey = dbUser + "+" + dbPath + "_" + (int) (Math.random() * 100.f);
		
		rootDbCon = this;
	}

	private DatabaseConnector(MainHttpListener listener, String dbPath, String dbUser, String dbPassword,
			String prefix, DatabaseConnector rootDbCon) {
		this.dbPath = dbPath;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;

		dbPrefix = prefix;

		this.listener = listener;

		uniqueKey = dbUser + "+" + dbPath + "_" + (int) (Math.random() * 100.f);
		
		this.rootDbCon = rootDbCon;
	}

	public DatabaseConnector getSubmoduleConnector(String subModuleName) {
		String totalPrefix = "";

		if (!dbPrefix.equals("")) {
			totalPrefix += dbPrefix + "_";
		}

		totalPrefix += subModuleName + "_";

		return new DatabaseConnector(listener, dbPath, dbUser, dbPassword, totalPrefix, rootDbCon);
	}

	private Connection openConnection() throws SQLException {
		return DriverManager.getConnection(dbPath, dbUser, dbPassword);
	}

	public void executeUpdatePreparedQuery(List<DbValue> values, String preparedStatementString) {
		try (Connection con = openConnection();
				PreparedStatement stmt = con.prepareStatement(preparedStatementString)) {

			addParametersToStatement(values, stmt, con);

			logger.log(Level.INFO, stmt.toString());

			stmt.executeUpdate();
			// con.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void insert(String dbName, List<String> columns, List<DbValue> values) {
		StringBuilder sb = new StringBuilder();

		sb.append("INSERT INTO " + dbPrefix + dbName + " (");

		sb.append(generateCommaSeperatedList(columns));

		sb.append(") VALUES (");

		sb.append(generateCommaSeperatedQuestionMarks(values.size()));

		sb.append(")");

		executeUpdatePreparedQuery(values, sb.toString());
	}

	public List<Map<String, DbValue>> select(String dbName, Map<String, DbValueType> returnTypes) {
		return select(dbName, Arrays.asList(), Arrays.asList(), returnTypes, null);
	}

	public List<Map<String, DbValue>> select(String dbName, Map<String, DbValueType> returnTypes, String append) {
		return select(dbName, Arrays.asList(), Arrays.asList(), returnTypes, append);
	}

	public List<Map<String, DbValue>> select(String dbName, String columns, DbValue selector,
			Map<String, DbValueType> returnTypes) {
		return select(dbName, Arrays.asList(columns), Arrays.asList(selector), returnTypes);
	}

	public List<Map<String, DbValue>> select(String dbName, List<String> columns, List<DbValue> selector,
			Map<String, DbValueType> returnTypes) {
		return select(dbName, columns, selector, returnTypes, "");
	}

	public List<Map<String, DbValue>> select(String dbName, String columns, DbValue selector,
			Map<String, DbValueType> returnTypes, String append) {
		return select(dbName, Arrays.asList(columns), Arrays.asList(selector), returnTypes, append);
	}

	public List<Map<String, DbValue>> select(String dbName, List<String> columns, List<DbValue> selector,
			Map<String, DbValueType> returnTypes, String append) {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM " + dbPrefix + dbName + " WHERE ");

		for (String c : columns) {
			sb.append(c + " = ? AND ");
		}

		sb.append(" TRUE " + (append == null ? "" : append) + " ;");

		List<Map<String, DbValue>> retList = new ArrayList<>();

		try (Connection con = openConnection(); PreparedStatement stmt = con.prepareStatement(sb.toString())) {

			addParametersToStatement(selector, stmt, con);

			logger.log(Level.INFO, stmt.toString());

			try (ResultSet rs = stmt.executeQuery()) {
				ResultSetMetaData rsmd = rs.getMetaData();

				Map<String, Integer> columnIdMap = new HashMap<>();

				for (int j = 1; j < (rsmd.getColumnCount() + 1); j++) {
					columnIdMap.put(rsmd.getColumnName(j), new Integer(j));
				}

				while (rs.next()) {
					Map<String, DbValue> tmpMap = new HashMap<>();

					for (Entry<String, DbValueType> e : returnTypes.entrySet()) {

						int columnNumber = columnIdMap.get(e.getKey()).intValue();

						switch (e.getValue()) {
						case BLOB:
							tmpMap.put(e.getKey(), new DbValue(rs.getBlob(columnNumber)));
							break;

						case STRING:
							tmpMap.put(e.getKey(), new DbValue(rs.getString(columnNumber)));
							break;

						case INTEGER:
							tmpMap.put(e.getKey(), new DbValue(rs.getInt(columnNumber)));
							break;

						case REAL:
							tmpMap.put(e.getKey(), new DbValue(rs.getDouble(columnNumber)));
							break;

						case TIMESTAMP:
							final long timestampMillis = rs.getLong(columnNumber);
							final DbValue dbValue = timestampMillis > 0 ? new DbValue(new Timestamp(timestampMillis)) : null;
							tmpMap.put(e.getKey(), dbValue);
							break;

						default:
							throw new RuntimeException("unknown value type in DatabaseConnector::executePreparedQuery");

						}
					}
					retList.add(tmpMap);

				}

			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return retList;
	}

	private static void addParametersToStatement(List<DbValue> selector, PreparedStatement stmt, Connection con)
			throws SQLException {
		int i = 1;
		for (DbValue v : selector) {
			switch (v.getDbValueType()) {
			case BLOB:
				stmt.setBlob(i, v.getBlob(con));
				break;

			case STRING:
				stmt.setString(i, v.getString());
				break;

			case INTEGER:
				stmt.setInt(i, v.getInt());
				break;

			case REAL:
				stmt.setDouble(i, v.getDouble());
				break;

			case TIMESTAMP:
				final long time = v.getTimestamp() == null ? -1 : v.getTimestamp().getTime();
				stmt.setLong(i, time);
				break;

			default:
				throw new RuntimeException("unknown value type in DatabaseConnector::executePreparedQuery");
			}
			i++;
		}
	}

	private static String generateCommaSeperatedList(List<String> columns) {
		StringJoiner sj = new StringJoiner(",");

		for (String c : columns) {
			sj.add(c);
		}

		return sj.toString();
	}

	private static String generateCommaSeperatedQuestionMarks(int size) {
		StringJoiner sj = new StringJoiner(",");
		for (int i = 0; i < size; i++) {
			sj.add("?");
		}
		return sj.toString();
	}

	public void updateValue(String tableName, String valueColumns, DbValue valueValues, String selectorColumns,
			DbValue selectorIds) {
		updateValue(tableName, Arrays.asList(valueColumns), Arrays.asList(valueValues), Arrays.asList(selectorColumns),
				Arrays.asList(selectorIds));
	}

	public void updateValue(String tableName, List<String> valueColumns, List<DbValue> valueValues,
			String selectorColumns, DbValue selectorIds) {
		updateValue(tableName, valueColumns, valueValues, Arrays.asList(selectorColumns), Arrays.asList(selectorIds));
	}

	public void updateValue(String tableName, List<String> valueColumns, List<DbValue> valueValues,
			List<String> selectorColumns, List<DbValue> selectorIds) {

		StringBuilder sb = new StringBuilder();

		sb.append("UPDATE " + dbPrefix + tableName + " SET ");

		StringJoiner sj = new StringJoiner(" , ");

		for (String c : valueColumns) {
			sj = sj.add(c + " = ?");
		}

		sb.append(sj.toString());

		sb.append(" WHERE ");

		for (String c : selectorColumns) {
			sb.append(c + " = ? AND ");
		}

		sb.append(" TRUE;");

		String query = sb.toString();
		// logger.log(Level.INFO, query);

		try (Connection con = openConnection(); PreparedStatement stmt = con.prepareStatement(query)) {
			List<DbValue> allParams = new ArrayList<>();
			allParams.addAll(valueValues);
			allParams.addAll(selectorIds);

			addParametersToStatement(allParams, stmt, con);

			logger.log(Level.INFO, stmt.toString());

			stmt.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public void deleteFrom(String tableName, String selectorColumn, DbValue selectorIds) {
		deleteFrom(tableName, Arrays.asList(selectorColumn), Arrays.asList(selectorIds));
	}

	public void deleteFrom(String tableName, List<String> selectorColumns, List<DbValue> selectorIds) {

		StringBuilder sb = new StringBuilder();

		sb.append("DELETE FROM " + dbPrefix + tableName + " WHERE ");

		for (String c : selectorColumns) {
			sb.append(c + " = ? AND ");
		}

		sb.append(" TRUE;");

		String query = sb.toString();

		try (Connection con = openConnection(); PreparedStatement stmt = con.prepareStatement(query)) {
			addParametersToStatement(selectorIds, stmt, con);

			logger.log(Level.INFO, stmt.toString());

			stmt.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public MainHttpListener getListener() {
		return listener;
	}

	public static void initializeDatabase(DatabaseConnector dbCon) {
		DbUtil.createAllTables(dbCon);

		for (Entry<String, AbstractModule> e : dbCon.getListener().getReqHandler().moduleMap.entrySet()) {
			DbUtil.createAllTablesForModule(dbCon.getSubmoduleConnector(e.getKey()),
					"co.clund.submodule." + e.getKey());
		}

		final DatabaseConnector coreSubModuleCon = dbCon.getSubmoduleConnector(Core.CORE_LOCATION);
		TConfiguration.initializeDefaultConfig(coreSubModuleCon);

		TBlock.initializeDefaultBlocks(coreSubModuleCon);

		TMenuStructure.initializeDefaultStructure(coreSubModuleCon);

		TSiteTemplate.initializeDefaultTemplate(coreSubModuleCon);
		
		TBlockRegionRelation.initializeDefaultTBlockRegionRelation(coreSubModuleCon);
	}
}
