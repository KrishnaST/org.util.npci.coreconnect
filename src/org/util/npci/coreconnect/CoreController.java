package org.util.npci.coreconnect;

import org.util.npci.api.BankController;
import org.util.npci.api.ConfigurationNotFoundException;
import org.util.npci.api.model.BankConfig;

public final class CoreController extends BankController {

	private final CoreConfig coreConfig;

	public CoreController(final BankConfig bankConfig) throws ConfigurationNotFoundException {
		this.coreConfig = new CoreConfig(bankConfig);
	}

	@Override
	public boolean shutdown() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BankConfig getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
