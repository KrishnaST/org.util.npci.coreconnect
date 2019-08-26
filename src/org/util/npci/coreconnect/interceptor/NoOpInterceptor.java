package org.util.npci.coreconnect.interceptor;

import org.util.iso8583.ISO8583Message;

public final class NoOpInterceptor implements Interceptor {

	private final InterceptorType type;

	public NoOpInterceptor(InterceptorType type) {
		this.type = type;
	}

	@Override
	public final String name() {
		return "NoOp";
	}

	@Override
	public final InterceptorType type() {
		return type;
	}

	@Override
	public final void applyToRequest(ISO8583Message request) {
		
	}

	@Override
	public final void applyToResponse(ISO8583Message response) {
		
	}

	
}
