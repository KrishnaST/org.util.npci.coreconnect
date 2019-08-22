package org.util.npci.coreconnect.acquirer;

import org.util.npci.api.ShutDownable;
import org.util.npci.api.model.AcquirerConfig;
import org.util.npci.coreconnect.CoreConfig;

public abstract class AcquirerServer extends Thread implements ShutDownable {

	public final AcquirerConfig acquirerConfig;
	public final CoreConfig     config;

	public AcquirerServer(final AcquirerConfig acquirerConfig, final CoreConfig config) {
		this.acquirerConfig = acquirerConfig;
		this.config         = config;
		setName(config.bankId + "-acqserver");
	}

	public abstract void run();

	public abstract String getServerType();
	
	public abstract String getAcquirerStatus();
}
