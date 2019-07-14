package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583LogSupplier;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.format.ISOFormat;
import org.util.iso8583.format.NPCIFormat;
import org.util.nanolog.Logger;
import org.util.nanolog.LoggerType;

public abstract class IssuerTransaction<T extends IssuerDispatcher> implements Runnable {

	protected static final ISOFormat npciFormat = NPCIFormat.getInstance();

	protected final ISO8583Message request;
	protected final T              dispatcher;

	public IssuerTransaction(final ISO8583Message request, final T dispatcher) {
		this.request    = request;
		this.dispatcher = dispatcher;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public final void run() {
		try (Logger logger = Logger.getLogger(LoggerType.BUFFERED, dispatcher.config.issWriter)) {
			if (request == null) return;
			logger.info("issuer class : " + getClass().getName());
			logger.info("issuer request ", new ISO8583LogSupplier(request));
			execute(logger);
			logger.info(new ISO8583LogSupplier(request));
		} catch (Exception e) {
			Logger.getConsole().info(e);
		}
	}
}
