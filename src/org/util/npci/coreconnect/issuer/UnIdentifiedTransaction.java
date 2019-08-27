package org.util.npci.coreconnect.issuer;

import org.util.datautil.TLV;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.npci.IMPSResponseCode;
import org.util.nanolog.Logger;

public final class UnIdentifiedTransaction extends IssuerTransaction<IssuerDispatcher> {

	public UnIdentifiedTransaction(final ISO8583Message request, final IssuerDispatcher dispatcher) {
		super(request, dispatcher);
	}

	@Override
	protected boolean execute(final Logger logger) {
		try {
			final TLV DE120 = TLV.parse(request.get(120));
 			logger.info("Request DE120", DE120.toString());
			request.put(39, IMPSResponseCode.INVALID_TRANSACTION);
			return sendResponseToNPCI(request, IMPSResponseCode.INVALID_TRANSACTION, logger);
		} catch (Exception e) {logger.info(e);}
		return false;
	}

}
