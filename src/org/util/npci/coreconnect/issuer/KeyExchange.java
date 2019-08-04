package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;

public abstract class KeyExchange<T extends IssuerDispatcher> extends IssuerTransaction<T>  {

	public KeyExchange(final ISO8583Message request, final T dispatcher) {
		super(request, dispatcher);
	}

	@Override
	protected final void execute(Logger logger) {
		logger.info("key change request from npci.");
		final String zpk_zmk 	= request.get(48).substring(0, 32);
		final String kcv 		= request.get(48).substring(32);
	}
	
	public abstract String getZMK();
	
	public abstract void processKey();

}
