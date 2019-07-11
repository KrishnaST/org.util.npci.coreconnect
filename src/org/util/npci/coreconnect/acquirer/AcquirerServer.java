package org.util.npci.coreconnect.acquirer;

import org.util.npci.api.ShutDownable;
import org.util.npci.coreconnect.CoreConfig;

public abstract class AcquirerServer extends Thread implements ShutDownable {

	public final CoreConfig config;

	public AcquirerServer(CoreConfig config) {
		this.config = config;
	}
	
	public abstract void run();
	
	public abstract String getServerType();


	

}
