package co.clund.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RatelimitAbidingThreadPoolExecutor extends ThreadPoolExecutor {

	private final static int corePoolSize = 9;
	private final static int maximumPoolSize = 50;
	private final static long keepAliveTime = 5;
	private final static TimeUnit unit = TimeUnit.SECONDS;

	private final Timer rateLimitTimer = new Timer();

	private final int rateLimiterScheduleFrequency = 10;

	/**
	 * @param rateLimit
	 *            in requests per second
	 */
	public RatelimitAbidingThreadPoolExecutor(int rateLimit) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>());

		/*final RatelimitAbidingThreadPoolExecutor tmpMother = this;
		final Timer tmpRateLimitTimer = rateLimitTimer;

		final TimerTask task = new TimerTask() {

			long lastAmountFinished = 0;

			@Override
			public void run() {

				long currentlyFinished = tmpMother.getActiveCount() + tmpMother.getCompletedTaskCount();

				long inLastIntervallFinished = currentlyFinished - lastAmountFinished;

				if (inLastIntervallFinished > (rateLimit / rateLimiterScheduleFrequency)) {
					long millisToWait = 1000 * inLastIntervallFinished / rateLimit
							- 1000 / rateLimiterScheduleFrequency;

					tmpMother.pause();
					try {
						Thread.sleep(millisToWait);
					} catch (@SuppressWarnings("unused") InterruptedException e) {
						// nothing to do here
					}
					tmpMother.resume();
				}

				lastAmountFinished = currentlyFinished;

				tmpRateLimitTimer.schedule(this, 1000 / rateLimiterScheduleFrequency);
			}

		};

		rateLimitTimer.schedule(task, 1000 / rateLimiterScheduleFrequency);*/
	}

	private boolean isPaused;
	private ReentrantLock pauseLock = new ReentrantLock();
	private Condition unpaused = pauseLock.newCondition();

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);

		pauseLock.lock();
		try {
			while (isPaused)
				unpaused.await();
		} catch (@SuppressWarnings("unused") InterruptedException ie) {
			t.interrupt();
		} finally {
			pauseLock.unlock();
		}
	}

	protected void pause() {
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	protected void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	Map<String, FutureTask<?>> taskMap = new HashMap<>();

	public FutureTask<?> insertTask(String identifier, FutureTask<?> task) {
		if (taskMap.containsKey(identifier)) {
			return taskMap.get(identifier);
		}

		this.execute(task);

		return task;
	}
}
