package org.util.npci.coreconnect;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.util.npci.api.ShutDownable;
import org.util.npci.coreconnect.internals.CustomThreadFactory;

public final class Schedular implements ShutDownable {

	private final ThreadFactory               threadFactory;
	private final ScheduledThreadPoolExecutor schedular;
	private final ThreadPoolExecutor          executor;

	public Schedular(CoreConfig config) {
		threadFactory = new CustomThreadFactory(config.bankId);
		schedular     = new ScheduledThreadPoolExecutor(5, threadFactory);
		executor      = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
		schedular.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		schedular.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		schedular.setRemoveOnCancelPolicy(true);

	}

	public final ScheduledFuture<?> scheduleWithFixedDelay(final Runnable runnable, final long initialDelay, final long delay, final TimeUnit unit) {
		return schedular.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
	}

	public final ScheduledFuture<?> scheduleAtFixedRate(final Runnable runnable, final long initialDelay, final long delay, final TimeUnit unit) {
		return schedular.scheduleAtFixedRate(runnable, initialDelay, delay, unit);
	}
	
	public final boolean execute(Runnable runnable) {
		executor.execute(runnable);
		return true;
	}
	
	public final Future<?> submit(Runnable runnable) {
		return executor.submit(runnable);
	}
	
	@Override
	public final boolean shutdown() {
		try {
			schedular.shutdownNow();
			executor.shutdown();
			return true;
		} catch (Exception e) {}
		return false;
	}

}
