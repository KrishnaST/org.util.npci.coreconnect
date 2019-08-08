package org.util.npci.coreconnect.issuer;

import org.util.hsm.api.model.MACResponse;
import org.util.iso8583.ISO8583LogSupplier;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.format.ISOFormat;
import org.util.iso8583.format.NPCIFormat;
import org.util.iso8583.npci.MTI;
import org.util.iso8583.npci.ResponseCode;
import org.util.nanolog.Logger;
import org.util.npci.coreconnect.CoreConfig;
import org.util.npci.coreconnect.util.MACUtil;

public abstract class IssuerTransaction<T extends IssuerDispatcher> implements Runnable {

	protected static final ISOFormat npciFormat = NPCIFormat.getInstance();

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
				final MACResponse macResponse = MACUtil.validateMAC(config, request.getMAB(), "", logger);
				if (macResponse != null && macResponse.isSuccess) execute(logger);
				else {
					request.put(0, MTI.getResponseMTI(request.get(0)));
					request.put(39, ResponseCode.MAC_FAILURE_ISSUER);
					config.coreconnect.sendResponseToNPCI(request, logger);
				}
			}
			else execute(logger);
		} catch (Exception e) {
			config.corelogger.error(e);
		}
	}
}
