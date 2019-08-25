package org.util.npci.coreconnect.interceptor;

import java.util.List;
import java.util.ServiceLoader;

import org.util.npci.coreconnect.CoreConfig;

public abstract class InterceptorBuilder {

	public abstract List<String> names();
	
	public abstract Interceptor build(final CoreConfig config);
	
	public static final Interceptor getInterceptor(final CoreConfig config, final InterceptorType type, final String name) {
		final ServiceLoader<InterceptorBuilder> serviceLoader = ServiceLoader.load(InterceptorBuilder.class, InterceptorBuilder.class.getClassLoader());
		for(final InterceptorBuilder builder : serviceLoader) {if (builder.names().contains(name)) return builder.build(config); }
		return new NoOpInterceptor();
	}
}
