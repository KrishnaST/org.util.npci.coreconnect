package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583Message;
import org.util.npci.coreconnect.CoreConfig;

public class LogonDispatcher extends IssuerDispatcher {

	public LogonDispatcher(CoreConfig config) {
		super(config);

	}

	@Override
	public final String getName() {
		return "LOGON";
	}

	@Override
	public final boolean dispatch(ISO8583Message request) {
		return false;
	}

}