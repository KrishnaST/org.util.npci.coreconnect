package org.util.npci.coreconnect.interceptor;

import org.util.iso8583.ISO8583Message;

public final class NoOpInterceptor implements Interceptor {


	@Override
	public final String name() {
		return "NoOp";
	}

	@Override
	public final void applyToRequest(final ISO8583Message request) {
		
	}

	@Override
	public final void applyToResponse(final ISO8583Message response) {
		
	}

	@Override
	public InterceptorType type() {
		return InterceptorType.ISSUER;
	}

}
