package org.util.npci.coreconnect.acquirer;

import org.util.nanolog.Logger;
import org.util.npci.coreconnect.CoreConfig;

//@formatter:off
public abstract class NoLogAcquirerTransaction implements Runnable {

	protected final CoreConfig config;

	public NoLogAcquirerTransaction(final CoreConfig config) {
		this.config = config;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public final void run() {
		Thread.currentThread().setName(config.bankId + "-acq-" + getClass().getSimpleName());
		try {
			final Logger logger = Logger.CONSOLE;
			logger.info("acquirer class ", getClass().getName());
			execute(logger);
		} catch (final Exception e) {config.corelogger.error(e);}
	}

}
