package org.util.npci.coreconnect.acquirer;

import java.util.concurrent.atomic.AtomicInteger;

import org.util.nanolog.Logger;
import org.util.npci.coreconnect.CoreConfig;

public abstract class AcquirerTransaction implements Runnable {

	private static final AtomicInteger counter = new AtomicInteger(0);

	protected final CoreConfig config;

	public AcquirerTransaction(final CoreConfig config) {
		this.config = config;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public void run() {
		try(final Logger logger = config.getAcquirerLogger()) {
			Thread.currentThread().setName(config.bankId + "-acq-" + counter.getAndIncrement());
			logger.info("acquirer class ", getClass().getName());
			execute(logger);
			Thread.currentThread().setName("");
		} catch (final Exception e) {config.corelogger.error(e);}
	}

}
