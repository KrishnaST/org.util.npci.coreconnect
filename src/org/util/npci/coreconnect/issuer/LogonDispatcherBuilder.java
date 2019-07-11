package org.util.npci.coreconnect.issuer;

import java.util.List;

import org.util.npci.coreconnect.CoreConfig;

public final class LogonDispatcherBuilder extends IssuerDispatcherBuilder {

	@Override
	public final List<String> getDispatcherTypes() {
		return List.of("LOGON");
	}

	@Override
	public final IssuerDispatcher build(CoreConfig config) {
		return new LogonDispatcher(config);
	}

}
