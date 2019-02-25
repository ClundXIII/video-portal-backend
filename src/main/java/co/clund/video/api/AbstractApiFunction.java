package co.clund.video.api;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reflections.Reflections;

import co.clund.video.UserSession;
import co.clund.video.db.DatabaseConnector;
import co.clund.video.html.Builder;
import co.clund.video.module.FunctionResult;
import co.clund.video.util.log.LoggingUtil;

public abstract class AbstractApiFunction {

	protected final static String GET_PARAM_COMMUNITY_TOKEN = "community_token";
	protected final static String GET_PARAM_INTERNAL_CHANNEL_IDS = "internal_channel_ids";

	public static List<AbstractApiFunction> getAllFunctions(DatabaseConnector dbCon) {

		List<AbstractApiFunction> retList = new ArrayList<>();

		final Logger logger = LoggingUtil.getApiLogger();

		Reflections reflections = new Reflections("co.clund.video.api");
		Set<Class<? extends AbstractApiFunction>> allClasses = reflections.getSubTypesOf(AbstractApiFunction.class);

		for (Class<? extends AbstractApiFunction> c : allClasses) {
			logger.log(Level.INFO, "loading AbstractApiFunction class " + c.getName());
			try {
				Constructor<? extends AbstractApiFunction> cons = c.getConstructor(DatabaseConnector.class);
				AbstractApiFunction r = cons.newInstance(new Object[] { dbCon });
				retList.add(r);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

		return retList;
	}

	private final String functionName;
	protected final DatabaseConnector dbCon;
	protected final Logger logger;

	AbstractApiFunction(DatabaseConnector dbCon, String functionName) {
		this.functionName = functionName;
		this.dbCon = dbCon;
		this.logger = LoggingUtil.getApiLogger();
	}

	public String getFunctionName() {
		return functionName;
	}

	public abstract FunctionResult execute(UserSession s, Map<String, String[]> parameters);

	public abstract Builder getDocumentation();
}
