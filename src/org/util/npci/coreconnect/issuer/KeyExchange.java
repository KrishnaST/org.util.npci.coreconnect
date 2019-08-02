package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;

public abstract class KeyExchange<T extends IssuerDispatcher> extends IssuerTransaction<T>  {

	public KeyExchange(final ISO8583Message request, final T dispatcher) {
		super(request, dispatcher);
	}

	@Override
	protected void execute(Logger logger) {
		
	}
	
	public abstract void processKey();

}
