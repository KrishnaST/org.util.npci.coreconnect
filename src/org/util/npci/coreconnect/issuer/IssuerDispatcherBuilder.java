package org.util.npci.coreconnect.issuer;

import java.util.List;
import java.util.ServiceLoader;

import org.util.npci.api.ConfigurationNotFoundException;
import org.util.npci.coreconnect.CoreConfig;

public abstract class IssuerDispatcherBuilder {

	public abstract List<String> getDispatcherTypes();

	public abstract IssuerDispatcher build(CoreConfig coreConfig) throws ConfigurationNotFoundException;

	public static final IssuerDispatcher getIssuerDispatcher(final CoreConfig config) throws ConfigurationNotFoundException {
		final ServiceLoader<IssuerDispatcherBuilder> serviceLoader = ServiceLoader.load(IssuerDispatcherBuilder.class, IssuerDispatcherBuilder.class.getClassLoader());
		for (IssuerDispatcherBuilder builder : serviceLoader) { if (builder.getDispatcherTypes().contains(config.dispatcherType)) return builder.build(config); }
		throw new ConfigurationNotFoundException("could not find dispatcher with name : " + config.dispatcherType);
	}
}
