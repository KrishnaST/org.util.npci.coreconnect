package org.util.npci.coreconnect.issuer;

import org.util.datautil.TLV;
import org.util.hsm.api.model.MACResponse;
import org.util.iso8583.ISO8583LogSupplier;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.npci.MTI;
import org.util.iso8583.npci.ResponseCode;
import org.util.nanolog.Logger;
import org.util.npci.coreconnect.CoreConfig;
import org.util.npci.coreconnect.util.MACUtil;

public abstract class IssuerTransaction<T extends IssuerDispatcher> implements Runnable {

	protected final ISO8583Message request;
	protected final T              dispatcher;
	protected final CoreConfig     config;

	public IssuerTransaction(final ISO8583Message request, final T dispatcher) {
		this.request    = request;
		this.dispatcher = dispatcher;
		this.config     = dispatcher.config;
	}

	protected abstract void execute(final Logger logger);

	@Override
	public final void run() {
		try(Logger logger = dispatcher.config.getIssuerLogger()) {
			if (request == null) return;
			logger.info("issuer class ", getClass().getName());
			logger.trace("issuer request ", new ISO8583LogSupplier(request));

			if (config.hasMAC && MTI.isMACable(request.get(0), request.get(3))) {
				final String macData = TLV.parse(request.get(48)).get("099");
				if(macData != null) {
					final MACResponse macResponse = MACUtil.validateMAC(config, request.getMAB(), macData, logger);
					logger.info("mac request", macResponse.toString());
					if (macResponse != null && macResponse.isSuccess) execute(logger);
					else sendResponseToNPCI(request, ResponseCode.MAC_FAILURE_ISSUER, logger);
				}
				else sendResponseToNPCI(request, ResponseCode.MAC_FAILURE_ISSUER, logger);
			}
			else execute(logger);
		} catch (Exception e) {config.corelogger.error(e);}
	}
	
	protected final void sendResponseToNPCI(final ISO8583Message response, final String responseCode, final Logger logger) {
		request.put(39, responseCode);
		if(request.get(39) == null) logger.error(new Exception("empty response code"));
		config.coreconnect.sendResponseToNPCI(request, logger);
	}
	
}
