package co.clund.util.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DynamicAsyncExpiringCache<T> implements Cache<T> {

	private static final Map<String, DynamicAsyncExpiringCacheEntry> data = new HashMap<>();

	public static final String SEPERATOR = ":@:";

	private final long lifetimeInMillis;
	private final String uuid;
	private static final Timer timer = new Timer();

	/**
	 * @param lifetime
	 *            seconds how long the content will stay
	 */
	public DynamicAsyncExpiringCache(String uuid, long lifetime) {
		this.lifetimeInMillis = lifetime * 1000;
		this.uuid = uuid;
	}

	@Override
	public void put(String key, T Value) {
		final String id = getFinalKey(key);

		final DynamicAsyncExpiringCacheEntry entry = new DynamicAsyncExpiringCacheEntry(Value.getClass(), Value, id);

		synchronized (data) {
			data.put(id, entry);
		}

		final DynamicAsyncExpiringCache<T> tmpMother = this;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				tmpMother.delete(id);
			}
		}, lifetimeInMillis);
	}

	@Override
	public boolean contains(String key) {
		return data.containsKey(getFinalKey(key));
	}

	@Override
	public void delete(String key) {
		synchronized (data) {
			data.remove(key);
		}
	}

	private String getFinalKey(String key) {
		return buildFinalKey(uuid, SEPERATOR, key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T retrieve(String key) {

		CacheEntry cacheEntry = data.get(getFinalKey(key));

		if (cacheEntry == null) {
			return null;
		}

		return (T) cacheEntry.getData();
	}

}
