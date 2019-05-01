package co.clund.html.block;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;

public abstract class AbstractBlock {
	private final String key;

	public AbstractBlock(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public abstract String render(DatabaseConnector dbCon, String pagePath, UserSession session, String content);

}
