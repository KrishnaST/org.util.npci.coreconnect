package org.util.npci.coreconnect;

import org.util.npci.api.BankController;
import org.util.npci.api.ConfigurationNotFoundException;
import org.util.npci.api.model.BankConfig;
import org.util.npci.coreconnect.acquirer.AcquirerServer;

public final class CoreController implements BankController {

	private final CoreConfig config;

	public CoreController(final BankConfig config) throws ConfigurationNotFoundException {
		this.config = new CoreConfig(config, this);
	}

	@Override
	public boolean shutdown() {
		final String bankId = config.bankId;
		for (AcquirerServer server : config.acquirers) {
			config.corelogger.error(bankId+ " : shutting down acquirer server "+server.getServerType()+" : "+server.shutdownQuietly());
		}
		config.corelogger.error(bankId+ " : shutting down datasource : "+closeQuietly(config.dataSource));
		config.corelogger.error(bankId+ " : shutting down schedular : "+config.schedular.shutdownQuietly());
		return false;
	}

	public final boolean isActive() {
		return config.coreconnect.isAlive();
	}

	@Override
	public final BankConfig getConfig() {
		return config;
	}
	
	private static final boolean closeQuietly(AutoCloseable closeable) {
		try {
			closeable.close();
			return true;
		} catch (Exception e) {}
		return false;
	}

	@Override
	public final void start() {
		final String bankId = config.bankId;
		for (AcquirerServer server : config.acquirers) {
			config.corelogger.info(bankId+ " : starting acquirer server "+server.getServerType());
			server.start();
		}
		config.corelogger.info(bankId+ " : starting coreconnect");
		config.coreconnect.start();
	}


}
