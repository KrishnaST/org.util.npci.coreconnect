package org.util.npci.coreconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.util.nanolog.Logger;
import org.util.npci.api.BankController;
import org.util.npci.api.ConfigurationNotFoundException;
import org.util.npci.api.model.BankConfig;
import org.util.npci.coreconnect.acquirer.AcquirerServer;
import org.util.npci.coreconnect.acquirer.AcquirerServerBuilder;
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
	private ScheduledFuture<?> logFuture;

	public CoreController(final BankConfig config) throws Exception {
		this.config = new CoreConfig(config, this);
	}

	@Override
	public final void start() {
		final String bankId = config.bankId;
		config.corelogger.info(bankId + " : starting coreconnect");
		config.coreconnect.start();
		logFuture 	= config.schedular.scheduleAtFixedRate((Runnable) Logger.CONSOLE, Logger.getEndOfDay(), 24*60*60, TimeUnit.SECONDS);
		logonFuture = config.schedular.scheduleWithFixedDelay(new ScheduledLogon(config), 5, 300, TimeUnit.SECONDS);
		echolFuture = config.schedular.scheduleWithFixedDelay(new ScheduledEchoLogon(config), 180, 180, TimeUnit.SECONDS);
		for (AcquirerServer server : config.acquirers) {
			config.corelogger.info(bankId + " : starting acquirer server " + server.getServerType());
			server.start();
		}
	}
	
	@Override
	public final void action(String action, Object... objects) throws ConfigurationNotFoundException, Exception {
		config.corelogger.info("action : " + action);
		if ("logon".equals(action)) config.schedular.execute(new Logon(config));
		else if ("echo-logon".equals(action)) config.schedular.execute(new EchoLogon(config));
		else if ("logoff".equals(action)) config.schedular.execute(new Logoff(config));
		else if ("key-exchange".equals(action)) config.schedular.execute(new ZPKRequest(config));
		else if ("toggle-console".equals(action)) Logger.setConsoleStatus(Logger.getConsoleStatus() ^ true);
		else if ("disable-auto-logon".equals(action)) {
			if(logonFuture != null) logonFuture.cancel(true);
			if(echolFuture != null) echolFuture.cancel(true);
			logonFuture = null;
			echolFuture = null;
		}
		else if ("enable-auto-logon".equals(action)) {
			if(logonFuture == null || logonFuture.isCancelled()) logonFuture = config.schedular.scheduleWithFixedDelay(new ScheduledLogon(config), 5, 300, TimeUnit.SECONDS);
			if(echolFuture == null || echolFuture.isCancelled()) echolFuture = config.schedular.scheduleWithFixedDelay(new ScheduledEchoLogon(config), 180, 180, TimeUnit.SECONDS);
		}
		else if ("start-acquirers".equals(action)) {
			final List<AcquirerServer> removables = new ArrayList<AcquirerServer>();
			final List<AcquirerServer> addables = new ArrayList<AcquirerServer>();
			for(final AcquirerServer acquirerServer : config.acquirers) {
				if(!acquirerServer.isAlive()) {
					config.corelogger.info(acquirerServer.getName(), acquirerServer.getAcquirerStatus());
					final AcquirerServer newServer = AcquirerServerBuilder.getAcquirerServer(acquirerServer.acquirerConfig, config);
					removables.add(acquirerServer);
					addables.add(newServer);
					newServer.start();
					config.corelogger.info(newServer.getAcquirerStatus());
				}
				else config.corelogger.info(acquirerServer.getServerType(), acquirerServer.getAcquirerStatus());
			}
			config.acquirers.removeAll(removables);
			config.acquirers.addAll(addables);
		}
		else if ("stop-acquirers".equals(action)) {
			for(final AcquirerServer acquirerServer : config.acquirers) {
				acquirerServer.shutdownQuietly();
				config.corelogger.info(acquirerServer.getAcquirerStatus());
			}
		}
		else if ("reset-npci-socket".equals(action)) {
			config.coreconnect.resetSocket();
		}
	}
	
	@Override
	public boolean shutdown() {
		final String bankId = config.bankId;
		for (AcquirerServer server : config.acquirers) {
			config.corelogger.error(bankId + " : shutting down acquirer server " + server.getServerType() + " : " + server.shutdownQuietly());
		}
		config.corelogger.error(bankId + " : shutting down datasource : " + closeQuietly((AutoCloseable) config.dataSource));
		config.corelogger.error(bankId + " : shutting down schedular : " + config.schedular.shutdownQuietly());
		if(logonFuture != null && !logonFuture.isCancelled()) logonFuture.cancel(true);
		if(echolFuture != null && !echolFuture.isCancelled()) echolFuture.cancel(true);
		config.corelogger.error(bankId + " : shutting down dispatcher : " + config.dispatcher.shutdownQuietly());
		config.corelogger.error(bankId + " : shutting down coreconnect : " + config.coreconnect.shutdownQuietly());
		config.corelogger.error(bankId + " : shutting down log date change schedular : " + logFuture.cancel(false));
		return true;
	}

	private static final boolean closeQuietly(AutoCloseable closeable) {
		try {
			closeable.close();
			return true;
		} catch (Exception e) {}
		return false;
	}

	public final boolean isActive() {
		return config.coreconnect.isAlive();
	}

	@Override
	public final BankConfig getConfig() {
		return config;
	}

}
