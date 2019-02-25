package co.clund.video.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;

import co.clund.video.UserSession;
import co.clund.video.db.DatabaseConnector;
import co.clund.video.module.FunctionResult.Status;
import co.clund.video.util.log.LoggingUtil;

public abstract class AbstractModule {

	protected final String MESSAGE_GET_VAR = "message";

	protected final String name;
	protected final DatabaseConnector dbCon;
	protected final Logger logger;

	public AbstractModule(String name, DatabaseConnector dbCon) {
		this.name = name;
		this.dbCon = dbCon;
		logger = LoggingUtil.getLoggerFromModule(getClass());
		functionMap = loadFunctions();
	}

	public String getModuleName() {
		return name;
	}

	public final byte[] invoke(final HttpServletResponse response, UserSession s, String function,
			Map<String, String[]> parameters) {
		try {
			if ((function == null) || function.equals("")) {
				return invokePlain(s, parameters);
			}

			BiFunction<UserSession, Map<String, String[]>, FunctionResult> f = functionMap.get(function);

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
}
