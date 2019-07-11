package org.util.npci.coreconnect.acquirer;

import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;
import org.util.nanolog.LoggerType;

public abstract class AcquirerTransaction<T extends AcquirerServer> implements Runnable {

	protected final ISO8583Message request;
	protected final T              acquirer;

	public AcquirerTransaction(final ISO8583Message request, final T acquirer) {
		this.request  = request;
		this.acquirer = acquirer;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public final void run() {
		try (Logger logger = Logger.getLogger(LoggerType.BUFFERED, acquirer.config.issWriter)) {
			logger.info("issuer class : " + getClass().getName());
			execute(logger);
		} catch (Exception e) {
			acquirer.config.coreLogger.error(e);
		}
	}

	public static void sendResponse(ISO8583Message issuerRespons, Logger logger) {
		try {
			//byte[] response = EncoderDecoder.encode(npciFormat, issuerResponse);
			//logger.info("sending response : " + ByteHexUtil.byteToHex(response));
			//NPCISocket.send(response, logger);
			return;
		} catch (Exception e) {
			logger.info(e);
		}
	}
}
