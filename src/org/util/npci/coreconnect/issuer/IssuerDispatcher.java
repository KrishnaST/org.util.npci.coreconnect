package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583Message;
import org.util.npci.api.ShutDownable;
import org.util.npci.coreconnect.CoreConfig;

public abstract class IssuerDispatcher implements ShutDownable {

	public final CoreConfig config;

	public IssuerDispatcher(final CoreConfig config) {
		this.config = config;
	}

	public abstract String getName();

	public abstract boolean dispatch(final ISO8583Message request);

}
