package org.util.npci.coreconnect.issuer;

import org.util.datautil.ByteHexUtil;
import org.util.iso8583.EncoderDecoder;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.format.ISOFormat;
import org.util.iso8583.format.NPCIFormat;
import org.util.iso8583.npci.MTI;
import org.util.nanolog.Logger;
import org.util.nanolog.LoggerType;

public abstract class IssuerTransaction<T extends IssuerDispatcher> implements Runnable {

	protected static final ISOFormat npciFormat = NPCIFormat.getInstance();

	protected final ISO8583Message request;
	protected final T              dispatcher;

	public IssuerTransaction(final ISO8583Message request, final T t) {
		this.request    = request;
		this.dispatcher = t;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public final void run() {
		try (Logger logger = Logger.getLogger(LoggerType.BUFFERED, dispatcher.config.issWriter)) {
			if (request == null) return;
			logger.info("issuer class : " + getClass().getName());
			execute(logger);
		} catch (Exception e) {
			Logger.getConsole().info(e);
		}
	}

	public static void sendResponse(ISO8583Message issuerResponse, MTI mti, String respCode, String authcode, String account, Logger logger) {
		try {
			issuerResponse.put(0, mti.value);
			issuerResponse.put(38, authcode);
			issuerResponse.put(39, respCode);
			issuerResponse.put(102, account);
			byte[] response = EncoderDecoder.encode(npciFormat, issuerResponse);
			logger.info("sending response : " + ByteHexUtil.byteToHex(response));
			//NPCISocket.send(response, logger);
			return;
		} catch (Exception e) {
			logger.info(e);
		}
	}
}
