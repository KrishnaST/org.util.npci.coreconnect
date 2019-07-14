package org.util.npci.coreconnect;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.util.nanolog.Logger;
import org.util.npci.api.BankController;
import org.util.npci.api.model.BankConfig;
import org.util.npci.coreconnect.acquirer.AcquirerServer;
import org.util.npci.coreconnect.logon.EchoLogon;
import org.util.npci.coreconnect.logon.Logoff;
import org.util.npci.coreconnect.logon.Logon;
import org.util.npci.coreconnect.logon.ScheduledEchoLogon;
import org.util.npci.coreconnect.logon.ScheduledLogon;
import org.util.npci.coreconnect.logon.ZPKRequest;

public final class CoreController implements BankController {

	private final CoreConfig config;

	private ScheduledFuture<?> logonFuture;
	private ScheduledFuture<?> echolFuture;

	public CoreController(final BankConfig config) throws Exception {
		this.config = new CoreConfig(config, this);
	}

	@Override
	public boolean shutdown() {
		final String bankId = config.bankId;
		for (AcquirerServer server : config.acquirers) {
			config.corelogger.error(bankId + " : shutting down acquirer server " + server.getServerType() + " : " + server.shutdownQuietly());
		}
		config.corelogger.error(bankId + " : shutting down datasource : " + closeQuietly(config.dataSource));
		config.corelogger.error(bankId + " : shutting down schedular : " + config.schedular.shutdownQuietly());
		logonFuture.cancel(true);
		echolFuture.cancel(true);
		config.corelogger.error(bankId + " : shutting down dispatcher : " + config.dispatcher.shutdownQuietly());
		config.corelogger.error(bankId + " : shutting down coreconnect : " + config.coreconnect.shutdownQuietly());
		return true;
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
			config.corelogger.info(bankId + " : starting acquirer server " + server.getServerType());
			server.start();
		}
		config.corelogger.info(bankId + " : starting coreconnect");
		config.coreconnect.start();
		logonFuture = config.schedular.scheduleWithFixedDelay(new ScheduledLogon(config), 5, 300, TimeUnit.SECONDS);
		echolFuture = config.schedular.scheduleWithFixedDelay(new ScheduledEchoLogon(config), 180, 180, TimeUnit.SECONDS);
	}

	@Override
	public final void action(String action, Object... objects) {
		config.corelogger.info("action : " + action);
		if ("logon".equals(action)) config.schedular.execute(new Logon(config));
		else if ("echo-logon".equals(action)) config.schedular.execute(new EchoLogon(config));
		else if ("logoff".equals(action)) config.schedular.execute(new Logoff(config));
		else if ("key-exchange".equals(action)) config.schedular.execute(new ZPKRequest(config));
		else if ("toggle-console".equals(action)) Logger.setConsoleStatus(Logger.getConsoleStatus() ^ true);
	}

}
