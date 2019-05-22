package co.clund.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Level;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.util.RatelimitAbidingThreadPoolExecutor;

public class Video extends AbstractModule {

	public static final String VIDEO_LOCATION = "video";

	private final Map<Integer, RatelimitAbidingThreadPoolExecutor> threadExecutorMap = new HashMap<>();

	public RatelimitAbidingThreadPoolExecutor getThreadExecutorMap(int platformId) {

		if (!threadExecutorMap.containsKey(new Integer(platformId))) {
			int ratelimit = 100;

			try {
				VideoPlatform plat = VideoPlatform.getPlatformById(dbCon, platformId);

				ratelimit = plat.getConfig().getInt(VideoPlatform.PLATFORM_JSON_CONFIG_RATELIMIT);
			} catch (Exception e) {
				logger.log(Level.WARNING, "no ratelimit set for platform with id " + platformId
						+ ", defaulting to 100!\n" + e.getMessage());
				e.printStackTrace();
			}

			threadExecutorMap.put(new Integer(platformId), new RatelimitAbidingThreadPoolExecutor(ratelimit));
		}

		return threadExecutorMap.get(new Integer(platformId));
	}

	public Video(@SuppressWarnings("unused") AbstractModule parent, DatabaseConnector dbCon) {
		super("", VIDEO_LOCATION, dbCon.getSubmoduleConnector(VIDEO_LOCATION));
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {
		return subModuleMap.get("index").invokePlain(s, parameters);
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		return new HashMap<>();
	}

	@Override
	public Map<String, AbstractModule> loadSubModules() {
		Map<String, AbstractModule> tmpSubModuleMap = new HashMap<>();

		Set<AbstractModule> subModuleSet = ModuleUtil.createSubModuleClasses(this, "co.clund.submodule.video", dbCon);

		for (AbstractModule m : subModuleSet) {
			tmpSubModuleMap.put(m.getModuleName(), m);
		}

		return tmpSubModuleMap;
	}
}
