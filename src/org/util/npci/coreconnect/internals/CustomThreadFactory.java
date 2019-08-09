package org.util.npci.coreconnect.internals;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.util.npci.coreconnect.acquirer.AcquirerTransaction;
import org.util.npci.coreconnect.acquirer.NoLogAcquirerTransaction;
import org.util.npci.coreconnect.issuer.IssuerTransaction;
import org.util.npci.coreconnect.issuer.NoLogIssuerTransaction;

public final class CustomThreadFactory implements ThreadFactory {

	private final String  prefix;
	private AtomicInteger counter = new AtomicInteger(0);

	public CustomThreadFactory(final String prefix) {
		this.prefix = prefix;
	}

	@Override
	public final Thread newThread(Runnable runnable) {
		if(runnable instanceof Scheduled || runnable instanceof Scheduled) return new Thread(runnable, getName(prefix, "sch", counter.getAndIncrement()));
		else if (runnable instanceof AcquirerTransaction || runnable instanceof NoLogAcquirerTransaction) return new Thread(runnable, getName(prefix, "acq", counter.getAndIncrement()));
		else if (runnable instanceof IssuerTransaction || runnable instanceof NoLogIssuerTransaction) return new Thread(runnable, getName(prefix, "iss", counter.getAndIncrement()));
		else return new Thread(runnable, getName(prefix, "gen", counter.getAndIncrement()));
	}

	private static final String getName(final String prefix, final String type, final int count) {
		return prefix + "-" + type + "-" + count;
	}

	
	public final String getPrefix() {
		return prefix;
	}
}
