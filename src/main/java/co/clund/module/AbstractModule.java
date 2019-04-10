package co.clund.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.module.FunctionResult.Status;
import co.clund.util.log.LoggingUtil;

public abstract class AbstractModule {

	protected final String MESSAGE_GET_VAR = "message";

	protected final String name;
	protected final String parentModule;
	protected final DatabaseConnector dbCon;
	protected final Logger logger;

	public AbstractModule(String parentModule, String name, DatabaseConnector dbCon) {
		this.name = name;
		this.parentModule = parentModule;
		this.dbCon = dbCon;
		logger = LoggingUtil.getLoggerFromModule(getClass());
		functionMap = loadFunctions();
		subModuleMap = loadSubModules();
	}

	public String getModuleName() {
		return name;
	}

	public String getModulePath() {
		return parentModule + "/" + name;
	}

	public String getparentModulePath() {
		return parentModule;
	}

	public final byte[] invoke(final HttpServletResponse response, UserSession s, String modulePath,
			Map<String, String[]> parameters) {
		try {
			if ((modulePath == null) || modulePath.equals("")) {
				return invokePlain(s, parameters);
			}

			if (modulePath.startsWith("/")) {

				String moduleRelativePath = modulePath.substring(1);

				int subModuleNameLength = -1;

				if (moduleRelativePath.indexOf("/") >= 0)
					subModuleNameLength = moduleRelativePath.indexOf("/");
				if (moduleRelativePath.indexOf(".") >= 0)
					subModuleNameLength = moduleRelativePath.indexOf(".");

				if (subModuleNameLength < 0)
					subModuleNameLength = moduleRelativePath.length();

				String subModuleName = moduleRelativePath.substring(0, subModuleNameLength);

				String subModulePath = moduleRelativePath.substring(subModuleName.length());

				if (subModuleMap.containsKey(subModuleName)) {
					AbstractModule invokedModule = subModuleMap.get(subModuleName);

					return (invokedModule.invoke(response, s, subModulePath, parameters));
				}
				return ("Not found:<br>" + subModuleName + "<br><a href='/'>return to main page</a>").getBytes();
			}

			String functionName = modulePath.substring(1); // remove the "." in front of the path the get the function
															// name

			BiFunction<UserSession, Map<String, String[]>, FunctionResult> f = functionMap.get(functionName);

			if (f == null) {
				return FunctionResult.Status.NOT_FOUND.name().getBytes();
			}

			FunctionResult r = f.apply(s, parameters);

			if (r.getStatus() == Status.DATA_RESPONSE) {
				return r.getData();
			}

			URIBuilder b = r.getBuilder();
			if (r.getStatus() != FunctionResult.Status.NONE) {
				b.addParameter(MESSAGE_GET_VAR, r.getMessage());
			}

			if (response != null) {
				response.addHeader("Location", b.build().toString());
				response.setStatus(HttpServletResponse.SC_FOUND);
			}

			return r.getStatus().name().getBytes();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage().getBytes();
		}
	}

	protected abstract byte[] invokePlain(UserSession s, Map<String, String[]> parameters);

	protected final Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> functionMap;

	protected abstract Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions();

	protected final Map<String, AbstractModule> subModuleMap;

	@SuppressWarnings("static-method")
	public Map<String, AbstractModule> loadSubModules() {
		return new HashMap<>();
	}

	public List<String> getFunctionList() {

		if (functionMap == null) {
			throw new RuntimeException("functionMap from " + getModuleName() + " has function Map null");
		}

		List<String> retList = new ArrayList<>();

		for (String s : functionMap.keySet()) {
			retList.add(s);
		}
		return retList;
	}

	public List<String> getSubModuleList() {

		if (subModuleMap == null) {
			throw new RuntimeException("getSubModuleList from " + getModuleName() + " has function Map null");
		}

		List<String> retList = new ArrayList<>();

		for (String s : subModuleMap.keySet()) {
			retList.add(s);
		}
		return retList;
	}
}
