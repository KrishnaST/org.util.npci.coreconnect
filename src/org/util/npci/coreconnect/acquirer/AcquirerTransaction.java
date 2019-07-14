package org.util.npci.coreconnect.acquirer;

import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;
import org.util.nanolog.LoggerType;
import org.util.npci.coreconnect.CoreConfig;

public abstract class AcquirerTransaction implements Runnable {

	protected final ISO8583Message request;
	protected final CoreConfig     config;

	public AcquirerTransaction(final ISO8583Message request, final CoreConfig config) {
		this.request = request;
		this.config  = config;
	}

	public AcquirerTransaction(final CoreConfig config) {
		this.request = new ISO8583Message();
		this.config  = config;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public final void run() {
		try (Logger logger = Logger.getLogger(LoggerType.BUFFERED, config.issWriter)) {
			execute(logger);
		} catch (Exception e) {config.corelogger.error(e);}
	}

}
