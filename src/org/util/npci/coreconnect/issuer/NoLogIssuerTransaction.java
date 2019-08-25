package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583LogSupplier;
import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;
import org.util.npci.coreconnect.CoreConfig;

public abstract class NoLogIssuerTransaction<T extends IssuerDispatcher> implements Runnable {

	protected final ISO8583Message request;
	protected final T              dispatcher;
	protected final CoreConfig     config;

	public NoLogIssuerTransaction(final ISO8583Message request, final T dispatcher) {
		this.request    = request;
		this.dispatcher = dispatcher;
		this.config     = dispatcher.config;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public final void run() {
		Thread.currentThread().setName(config.bankId + "-"+getClass().getSimpleName());
		try	{
			final Logger logger = Logger.CONSOLE;
			if (request == null) return;
			logger.info("issuer class ", getClass().getName());
			logger.trace("issuer request ", new ISO8583LogSupplier(request));
			execute(logger);
		} catch (final Exception e) {config.corelogger.error(e);}
		Thread.currentThread().setName("");
	}
	
	protected final boolean sendResponseToNPCI(final ISO8583Message response, final String responseCode, final Logger logger) {
		request.put(39, responseCode);
		if(request.get(39) == null) logger.error(new Exception("empty response code"));
		return config.coreconnect.sendResponseToNPCI(request, logger);
	}
	

}
