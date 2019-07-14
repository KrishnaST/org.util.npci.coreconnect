package org.util.npci.coreconnect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.util.nanolog.LogWriter;
import org.util.nanolog.Logger;
import org.util.nanolog.LoggerType;
import org.util.npci.api.BankController;
import org.util.npci.api.model.AcquirerConfig;
import org.util.npci.api.model.BankConfig;
import org.util.npci.coreconnect.acquirer.AcquirerServer;
import org.util.npci.coreconnect.acquirer.AcquirerServerBuilder;
import org.util.npci.coreconnect.issuer.IssuerDispatcher;
import org.util.npci.coreconnect.issuer.IssuerDispatcherBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class CoreConfig extends BankConfig {

	private static final long serialVersionUID = 1L;

	public final LogWriter issWriter;
	public final LogWriter acqWriter;
	public final Logger    corelogger;
	
	public final Schedular schedular;
	public final IssuerDispatcher dispatcher;
	public final HikariDataSource dataSource;
	public final List<AcquirerServer> acquirers;
	public final CoreConnect coreconnect;
	
	public CoreConfig(final BankConfig bankConfig, final BankController controller) throws Exception {
		super(bankConfig, controller);
		issWriter  = new LogWriter(bankConfig.bankId, "issuer_tx", true);
		acqWriter  = new LogWriter(bankConfig.bankId, "acquirer_tx", true);
		corelogger = Logger.getLogger(LoggerType.INSTANT, new LogWriter(bankConfig.bankId, "coreconnect", true));
		
		schedular = new Schedular(this);
		corelogger.info("schedular initialized : "+schedular);
		dispatcher = IssuerDispatcherBuilder.getIssuerDispatcher(this);
		corelogger.info("dispatcher initialized : "+dispatcher);
		dataSource = new HikariDataSource(new HikariConfig(this.dbProperties));
		corelogger.info("dataSource initialized : "+dataSource);
		acquirers = Collections.unmodifiableList(getAcquirerServerList());
		corelogger.info("acquirers initialized : "+acquirers);
		coreconnect = new CoreConnect(this);
		corelogger.info("coreconnect initialized : "+coreconnect);
	}

	
	private final List<AcquirerServer> getAcquirerServerList() throws Exception {
		final List<AcquirerServer> acquirers = new ArrayList<AcquirerServer>();
		for (AcquirerConfig acquirerConfig : acquirerConfigs) {
			acquirers.add(AcquirerServerBuilder.getAcquirerServer(acquirerConfig, this));
		}
		return acquirers;
	}
}
