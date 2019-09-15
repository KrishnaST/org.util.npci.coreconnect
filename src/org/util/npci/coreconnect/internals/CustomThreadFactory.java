package org.util.npci.coreconnect.internals;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.util.npci.coreconnect.CoreConfig;

public final class CustomThreadFactory implements ThreadFactory {

	private final String     prefix;
	private AtomicInteger    counter = new AtomicInteger(0);

	public CustomThreadFactory(final String prefix, final CoreConfig config) {
		this.prefix = prefix;
	}

	//@formatter:off
	@Override
	public final Thread newThread(Runnable runnable) {
		return new Thread(runnable, getName(prefix, counter.getAndIncrement()));
	}

	@SuppressWarnings("unused")
	private static final String getName(final String prefix, final String type, final int count) {
		return prefix + "-" + type + "-" + count;
	}
	
	private static final String getName(final String prefix, final int count) {
		return prefix + "-" + count;
	}
	
	public final String getPrefix() {
		return prefix;
	}
}
