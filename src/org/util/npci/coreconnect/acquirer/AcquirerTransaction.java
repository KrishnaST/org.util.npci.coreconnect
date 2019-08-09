package org.util.npci.coreconnect.acquirer;

import org.util.nanolog.Logger;
import org.util.npci.coreconnect.CoreConfig;

public abstract class AcquirerTransaction implements Runnable {

	protected final CoreConfig     config;

	public AcquirerTransaction(final CoreConfig config) {
		this.config  = config;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public void run() {
		try (final Logger logger = config.getAcquirerLogger()) {
			logger.info("acquirer class ", getClass().getName());
			execute(logger);
		} catch (final Exception e) {
			config.corelogger.error(e);
		}
	}

}
