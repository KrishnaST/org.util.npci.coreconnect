package org.util.npci.coreconnect.acquirer;

import java.util.List;
import java.util.ServiceLoader;

import org.util.npci.api.ConfigurationNotFoundException;
import org.util.npci.coreconnect.CoreConfig;

public abstract class AcquirerServerBuilder {

	public abstract List<String> getSupportedServerTypes();

	public abstract AcquirerServer build(CoreConfig coreConfig);

	public static final AcquirerServer getAcquirerServer(final String serverType, final CoreConfig config) throws ConfigurationNotFoundException {
		final ServiceLoader<AcquirerServerBuilder> serviceLoader = ServiceLoader.load(AcquirerServerBuilder.class, AcquirerServerBuilder.class.getClassLoader());
		for (AcquirerServerBuilder builder : serviceLoader) { if (builder.getSupportedServerTypes().contains(serverType)) return builder.build(config); }
		throw new ConfigurationNotFoundException("could not find acquirer server with name : "+serverType);
	}
}
