package org.util.npci.coreconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.util.hsm.api.HSMConfig;
import org.util.hsm.api.HSMService;
import org.util.nanolog.LogWriter;
import org.util.nanolog.Logger;
import org.util.nanolog.LoggerType;
import org.util.npci.api.BankController;
import org.util.npci.api.model.AcquirerConfig;
import org.util.npci.api.model.BankConfig;
import org.util.npci.coreconnect.acquirer.AcquirerServer;
import org.util.npci.coreconnect.acquirer.AcquirerServerBuilder;
import org.util.npci.coreconnect.interceptor.Interceptor;
import org.util.npci.coreconnect.interceptor.InterceptorBuilder;
import org.util.npci.coreconnect.interceptor.InterceptorType;
import org.util.npci.coreconnect.internals.CoreDatabaseServiceImpl;
import org.util.npci.coreconnect.internals.NoOpCoreDatabaseService;
import org.util.npci.coreconnect.issuer.IssuerDispatcher;
import org.util.npci.coreconnect.issuer.IssuerDispatcherBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class CoreConfig extends BankConfig {

	public final LogWriter issWriter;
	public final LogWriter acqWriter;
	public final Logger    corelogger;

	public final Schedular            schedular;
	public final HSMService           hsmService;
	public final IssuerDispatcher     dispatcher;
	public final DataSource           dataSource;
	public final List<AcquirerServer> acquirers;
	public final CoreConnect          coreconnect;
	public final HSMConfig            hsmConfig;
	public final Interceptor          issuerInterceptor;
	public final Interceptor          acquirerInterceptor;
	public final CoreDatabaseService  coreDatabaseService;

	public CoreConfig(final BankConfig bankConfig, final BankController controller) throws Exception {
		super(bankConfig, controller);
		issWriter  = new LogWriter(bankConfig.bankId, "issuer_tx", true);
		acqWriter  = new LogWriter(bankConfig.bankId, "acquirer_tx", true);
		corelogger = Logger.getLogger(LoggerType.INSTANT, new LogWriter(bankConfig.bankId, "coreconnect", true));

		issuerInterceptor   = InterceptorBuilder.getInterceptor(this, InterceptorType.ISSUER, getStringSupressException(PropertyName.ISSUER_INTERCEPTOR));
		acquirerInterceptor = InterceptorBuilder.getInterceptor(this, InterceptorType.ACQUIRER, getStringSupressException(PropertyName.ACQUIRER_INTERCEPTOR));

		hsmService = HSMService.getService("THALES");
		if (defaultHSMConfig != null) {
			this.hsmConfig = HSMConfig.newBuilder(defaultHSMConfig.host, defaultHSMConfig.port).withDecimalizationTable(defaultHSMConfig.decTab)
					.withLengthOfPinLMK(defaultHSMConfig.lengthOfPinLMK).withMaximumPinLength(defaultHSMConfig.maximumPinLength)
					.withMinimumPinLength(defaultHSMConfig.minimumPinLength).build();
		} else this.hsmConfig = null;

		schedular = new Schedular(this);
		corelogger.info("schedular initialized : " + schedular);
		dispatcher = IssuerDispatcherBuilder.getIssuerDispatcher(this);
		corelogger.info("dispatcher initialized : " + dispatcher.getName());
		if (!this.dbProperties.isEmpty()) {
			this.dbProperties.put("poolName", "hikari-datasource-" + bankId);
			dataSource = new HikariDataSource(new HikariConfig(this.dbProperties));
			corelogger.info("dataSource initialized : " + dataSource);
		} else {
			dataSource = null;
			corelogger.info("dataSource not initialized : " + dataSource);
		}
		if (bankConfig.isAcquirer) {
			acquirers = getAcquirerServerList();
			corelogger.info("acquirers initialized : " + acquirers.stream().map(acquirer -> acquirer.acquirerConfig.acquirerName).collect(Collectors.toList()));
		} else acquirers = List.of();

		coreDatabaseService = getBooleanSupressException(PropertyName.CORE_DATABASE_SERVICE) ? new CoreDatabaseServiceImpl(this) : new NoOpCoreDatabaseService();
		corelogger.info("core database service is initialized : " + coreDatabaseService);
		coreconnect = new CoreConnect(this);
		corelogger.info("coreconnect initialized : " + coreconnect);

	}

	private final List<AcquirerServer> getAcquirerServerList() throws Exception {
		final List<AcquirerServer> acquirers = new ArrayList<AcquirerServer>();
		for (final AcquirerConfig acquirerConfig : acquirerConfigs) { acquirers.add(AcquirerServerBuilder.getAcquirerServer(acquirerConfig, this)); }
		return acquirers;
	}

	public final Logger getIssuerLogger() {
		return Logger.getLogger(LoggerType.BUFFERED, issWriter);
	}

	public final Logger getAcquirerLogger() {
		return Logger.getLogger(LoggerType.BUFFERED, acqWriter);
	}

}
