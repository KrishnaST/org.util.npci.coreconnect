package org.util.npci.coreconnect.issuer;

import java.util.List;
import java.util.ServiceLoader;

import org.util.npci.coreconnect.CoreConfig;


public abstract class IssuerDispatcherBuilder {

	public abstract List<String> getDispatcherTypes();
	
	public abstract IssuerDispatcher build(CoreConfig coreConfig);
	
	public static final IssuerDispatcher getIssuerDispatcher(final CoreConfig config) {
		final ServiceLoader<IssuerDispatcherBuilder> serviceLoader = ServiceLoader.load(IssuerDispatcherBuilder.class, IssuerDispatcherBuilder.class.getClassLoader());
		for (IssuerDispatcherBuilder builder : serviceLoader) {
			if(builder.getDispatcherTypes().contains(config.dispatcherType)) return builder.build(config);
		}
		return null;
	}
}