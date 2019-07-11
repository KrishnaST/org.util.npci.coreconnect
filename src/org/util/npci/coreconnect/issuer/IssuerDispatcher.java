package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583Message;
import org.util.npci.coreconnect.CoreConfig;

public abstract class IssuerDispatcher {

	public final CoreConfig config;

	public IssuerDispatcher(CoreConfig config) {
		this.config = config;
	}
	
	public abstract String getName();

	public abstract String dispatch(final ISO8583Message request);

	

}
