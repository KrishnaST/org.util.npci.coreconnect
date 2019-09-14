package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583Message;
import org.util.iso8583.npci.IMPSResponseCode;
import org.util.nanolog.Logger;
import org.util.npci.coreconnect.util.NPCIISOUtil;

public final class UnIdentifiedTransaction extends IssuerTransaction<IssuerDispatcher> {

	public UnIdentifiedTransaction(final ISO8583Message request, final IssuerDispatcher dispatcher) {
		super(request, dispatcher);
	}

	@Override
	protected final boolean execute(final Logger logger) {
		try {
			request.put(39, IMPSResponseCode.INVALID_TRANSACTION);
			NPCIISOUtil.removeNotRequiredElements(request);
			return config.coreconnect.sendResponseToNPCI(request, logger);
		} catch (Exception e) {logger.info(e);}
		return false;
	}

}
