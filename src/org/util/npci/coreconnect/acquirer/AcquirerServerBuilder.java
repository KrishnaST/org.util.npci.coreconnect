package org.util.npci.coreconnect.acquirer;

import java.util.List;
import java.util.ServiceLoader;

import org.util.npci.api.ConfigurationNotFoundException;
import org.util.npci.api.model.AcquirerConfig;
import org.util.npci.coreconnect.CoreConfig;

public abstract class AcquirerServerBuilder {

	public abstract List<String> getSupportedServerTypes();

	public abstract AcquirerServer build(final AcquirerConfig acquirerConfig, final CoreConfig config);

	public static final AcquirerServer getAcquirerServer(final AcquirerConfig acquirerConfig, final CoreConfig config) throws ConfigurationNotFoundException {
		final ServiceLoader<AcquirerServerBuilder> serviceLoader = ServiceLoader.load(AcquirerServerBuilder.class, AcquirerServerBuilder.class.getClassLoader());
		for (AcquirerServerBuilder builder : serviceLoader) { if (builder.getSupportedServerTypes().contains(acquirerConfig.acquirerType)) return builder.build(acquirerConfig, config); }
		throw new ConfigurationNotFoundException("could not find acquirer server with name : "+acquirerConfig.acquirerType);
	}
}
