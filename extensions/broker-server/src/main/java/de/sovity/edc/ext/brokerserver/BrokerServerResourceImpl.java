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

import de.sovity.edc.ext.brokerserver.api.BrokerServerResource;
import de.sovity.edc.ext.brokerserver.api.model.*;
import de.sovity.edc.ext.brokerserver.db.DslContextFactory;
import de.sovity.edc.ext.brokerserver.services.api.CatalogApiService;
import de.sovity.edc.ext.brokerserver.services.api.ConnectorApiService;
import de.sovity.edc.ext.brokerserver.services.api.DataOfferDetailApiService;
import de.sovity.edc.ext.brokerserver.services.api.EventLogApiService;
import de.sovity.edc.ext.brokerserver.services.config.AdminApiKeyValidator;
import lombok.RequiredArgsConstructor;

import java.util.List;


/**
 * Implementation of {@link BrokerServerResource}
 */
@RequiredArgsConstructor
public class BrokerServerResourceImpl implements BrokerServerResource {
    private final DslContextFactory dslContextFactory;
    private final ConnectorApiService connectorApiService;
    private final CatalogApiService catalogApiService;
    private final DataOfferDetailApiService dataOfferDetailApiService;
    private final EventLogApiService eventLogApiService;
    private final AdminApiKeyValidator adminApiKeyValidator;

    @Override
    public CatalogPageResult catalogPage(CatalogPageQuery query) {
        return dslContextFactory.transactionResult(dsl -> catalogApiService.catalogPage(dsl, query));
    }

    @Override
    public ConnectorPageResult connectorPage(ConnectorPageQuery query) {
        return dslContextFactory.transactionResult(dsl -> connectorApiService.connectorPage(dsl, query));
    }

    @Override
    public DataOfferDetailPageResult dataOfferDetailPage(DataOfferDetailPageQuery query) {
        return dslContextFactory.transactionResult(dsl -> dataOfferDetailApiService.dataOfferDetailPage(dsl, query));
    }

    @Override
    public ConnectorDetailPageResult connectorDetailPage(ConnectorDetailPageQuery query) {
        return dslContextFactory.transactionResult(dsl -> connectorApiService.connectorDetailPage(dsl, query));
    }

    @Override
    public void addConnectors(List<String> endpoints, String adminApiKey) {
        adminApiKeyValidator.validateAdminApiKey(adminApiKey);
        dslContextFactory.transaction(dsl -> connectorApiService.addConnectors(dsl, endpoints));
    }


    @Override
    public EventLogPageResult eventLogPage(EventLogPageQuery query) {
        return dslContextFactory.transactionResult(dsl -> eventLogApiService.eventLogPage(dsl, query));
    }
}
