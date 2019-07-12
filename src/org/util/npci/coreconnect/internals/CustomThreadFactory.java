package org.util.npci.coreconnect.internals;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.util.npci.coreconnect.acquirer.AcquirerTransaction;
import org.util.npci.coreconnect.issuer.IssuerTransaction;

public final class CustomThreadFactory implements ThreadFactory {

	private final String  prefix;
	private AtomicInteger counter = new AtomicInteger(0);

	public CustomThreadFactory(final String prefix) {
		this.prefix = prefix;
	}

	@Override
	public final Thread newThread(Runnable runnable) {
		if (runnable instanceof AcquirerTransaction) return new Thread(runnable, getName(prefix, "acq", counter.getAndIncrement()));
		else if (runnable instanceof IssuerTransaction) return new Thread(runnable, getName(prefix, "iss", counter.getAndIncrement()));
		else return new Thread(runnable, getName(prefix, "gen", counter.getAndIncrement()));
	}

	public static final String getName(final String prefix, final String type, final int count) {
		return prefix + "-" + type + "-" + count;
	}
}
