package org.util.npci.coreconnect;

import org.util.npci.api.BankController;
import org.util.npci.api.ConfigurationNotFoundException;
import org.util.npci.api.model.BankConfig;

public final class CoreController extends BankController {

	private final CoreConfig config;

	public CoreController(final BankConfig config) throws ConfigurationNotFoundException {
		this.config = new CoreConfig(config);
	}

	@Override
	public boolean shutdown() {
		return false;
	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public final BankConfig getConfig() {
		return config;
	}

}
