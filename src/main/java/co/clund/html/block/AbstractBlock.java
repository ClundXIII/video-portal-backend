package co.clund.html.block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reflections.Reflections;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.util.log.LoggingUtil;

public abstract class AbstractBlock {
	private final String key;

	private static Map<String, AbstractBlock> blockList = initializeBlockList();

	public AbstractBlock(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	protected abstract String render(DatabaseConnector dbCon, String pagePath, UserSession session, String content);

	public static String render(DatabaseConnector dbCon, String key, String pagePath, UserSession session,
			String content) {
		AbstractBlock block = blockList.get(key);
		return block.render(dbCon, pagePath, session, content);
	}

	private static Map<String, AbstractBlock> initializeBlockList() {

		Reflections reflections = new Reflections("co.clund.html.block");

		Set<Class<? extends AbstractBlock>> classes = reflections.getSubTypesOf(AbstractBlock.class);

		Map<String, AbstractBlock> retMap = new HashMap<>();

		Logger logger = LoggingUtil.getDefaultLogger();

		for (Class<? extends AbstractBlock> c : classes) {
			if (!Modifier.isAbstract(c.getModifiers())) {
				Constructor<? extends AbstractBlock> cons;
				try {
					cons = c.getConstructor();
					AbstractBlock m = cons.newInstance();
					logger.log(Level.INFO, "Adding block \"" + m.getKey() + "\" from class \"" + m.toString() + "\"");
					retMap.put(m.getKey(), m);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "error while instanciating block: " + c.getName());
					throw new RuntimeException(e);
				}
			}
		}

		return retMap;
	}

}
