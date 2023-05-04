package de.sovity.edc.ext.brokerserver;

import org.eclipse.edc.connector.api.management.configuration.ManagementApiConfiguration;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;

public class BrokerServerExtension implements ServiceExtension {

    public static final String EXTENSION_NAME = "BrokerServerExtension";

    @Inject
    private ManagementApiConfiguration managementApiConfiguration;

    @Inject
    private WebService webService;

    @Override
    public String name() {
        return EXTENSION_NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var wrapperExtensionContext = BrokerServerExtensionContextBuilder.buildContext();

        wrapperExtensionContext.jaxRsResources().forEach(resource ->
            webService.registerResource(managementApiConfiguration.getContextAlias(), resource));
    }
}
