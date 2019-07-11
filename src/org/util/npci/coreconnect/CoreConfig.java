package org.util.npci.coreconnect;

import org.util.nanolog.LogWriter;
import org.util.nanolog.Logger;
import org.util.nanolog.LoggerType;
import org.util.npci.api.ConfigurationNotFoundException;
import org.util.npci.api.model.BankConfig;
import org.util.npci.coreconnect.issuer.IssuerDispatcher;
import org.util.npci.coreconnect.issuer.IssuerDispatcherBuilder;

public final class CoreConfig extends BankConfig {

	private static final long serialVersionUID = 1L;

	public final LogWriter issWriter;
	public final LogWriter acqWriter;
	public final Logger    coreLogger;
	public final IssuerDispatcher dispatcher;

	public CoreConfig(final BankConfig bankConfig) throws ConfigurationNotFoundException {
		super(bankConfig);
		issWriter  = new LogWriter(bankConfig.bankId, "issuer_tx", true);
		acqWriter  = new LogWriter(bankConfig.bankId, "acquirer_tx", true);
		coreLogger = Logger.getLogger(LoggerType.INSTANT, new LogWriter(bankConfig.bankId, "coreconnect", true));
		dispatcher = IssuerDispatcherBuilder.getIssuerDispatcher(this);

	}

}
