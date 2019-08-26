package org.util.npci.coreconnect.interceptor;

import org.util.iso8583.ISO8583Message;

public interface Interceptor {

	public String name();
	
	public InterceptorType type();
	
	public void applyToRequest(final ISO8583Message request);
	
	public void applyToResponse(final ISO8583Message response);
	
}
