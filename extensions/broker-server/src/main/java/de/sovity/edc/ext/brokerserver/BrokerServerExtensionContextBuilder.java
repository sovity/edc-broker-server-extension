/*
 *  Copyright (c) 2023 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *
 */

package de.sovity.edc.ext.brokerserver;

import de.sovity.edc.ext.brokerserver.dao.stores.ConnectorQueries;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.sovity.edc.ext.brokerserver.dao.stores.ConnectorStore;
import de.sovity.edc.ext.brokerserver.dao.stores.ContractOfferStore;
import de.sovity.edc.ext.brokerserver.db.DataSourceFactory;
import de.sovity.edc.ext.brokerserver.db.DslContextFactory;
import de.sovity.edc.ext.brokerserver.services.BrokerServerInitializer;
import de.sovity.edc.ext.brokerserver.services.api.CatalogApiService;
import de.sovity.edc.ext.brokerserver.services.api.ConnectorApiService;
import de.sovity.edc.ext.brokerserver.services.api.PaginationMetadataUtils;
import de.sovity.edc.ext.brokerserver.services.refreshing.sender.DescriptionRequestSender;
import de.sovity.edc.ext.brokerserver.services.refreshing.sender.IdsMultipartExtendedRemoteMessageDispatcher;
import lombok.NoArgsConstructor;
import org.eclipse.edc.protocol.ids.api.multipart.dispatcher.sender.IdsMultipartSender;
import org.eclipse.edc.protocol.ids.spi.service.DynamicAttributeTokenService;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.http.EdcHttpClient;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.configuration.Config;


/**
 * Manual Dependency Injection.
 * <p>
 * We want to develop as Java Backend Development is done, but we have
 * no CDI / DI Framework to rely on.
 * <p>
 * EDC {@link Inject} only works in {@link BrokerServerExtension}.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BrokerServerExtensionContextBuilder {

    public static BrokerServerExtensionContext buildContext(
            Config config,
            Monitor monitor,
            EdcHttpClient httpClient,
            DynamicAttributeTokenService dynamicAttributeTokenService,
            ObjectMapper objectMapper
    ) {
        // Dao
        var dataSourceFactory = new DataSourceFactory(config);
        var dataSource = dataSourceFactory.newDataSource();
        var dslContextFactory = new DslContextFactory(dataSource);
        var connectorQueries = new ConnectorQueries();
        var contractOfferStore = new ContractOfferStore();

        // IDS Message Client
        var idsMultipartSender = new IdsMultipartSender(monitor, httpClient, dynamicAttributeTokenService, objectMapper);
        var remoteMessageDispatcher = new IdsMultipartExtendedRemoteMessageDispatcher(idsMultipartSender);
        var descriptionRequestSender = new DescriptionRequestSender();

        // Services
        var brokerServerInitializer = new BrokerServerInitializer(dslContextFactory, config);

        // UI Capabilities
        var paginationMetadataUtils = new PaginationMetadataUtils();
        var catalogApiService = new CatalogApiService(
                contractOfferStore,
                paginationMetadataUtils
        );
        var connectorApiService = new ConnectorApiService(
                dslContextFactory,
                connectorQueries,
                paginationMetadataUtils
        );
        var brokerServerResource = new BrokerServerResourceImpl(connectorApiService, catalogApiService);
        return new BrokerServerExtensionContext(remoteMessageDispatcher, brokerServerResource, brokerServerInitializer);
    }
}
