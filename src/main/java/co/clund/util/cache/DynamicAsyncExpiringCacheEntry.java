package co.clund.util.cache;

public class DynamicAsyncExpiringCacheEntry extends CacheEntry {
	private final String key;
	private final long createdTimeStamp;

	DynamicAsyncExpiringCacheEntry(Class<? extends Object> type, Object data, String key) {
			super(type, data);

			createdTimeStamp = System.currentTimeMillis();
			this.key = key;
		}

	public long getCreatedTimeStamp() {
		return createdTimeStamp;
	}

	public String getKey() {
		return key;
	}

}
