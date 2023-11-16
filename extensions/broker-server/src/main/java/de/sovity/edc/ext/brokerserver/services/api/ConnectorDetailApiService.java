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

package de.sovity.edc.ext.brokerserver.services.api;

import de.sovity.edc.ext.brokerserver.api.model.ConnectorDetailPageQuery;
import de.sovity.edc.ext.brokerserver.api.model.ConnectorDetailPageResult;
import de.sovity.edc.ext.brokerserver.dao.pages.connector.ConnectorDetailQueryService;
import de.sovity.edc.ext.brokerserver.dao.pages.connector.model.ConnectorDetailsRs;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.util.Objects;

@RequiredArgsConstructor
public class ConnectorDetailApiService {
    private final ConnectorDetailQueryService connectorDetailQueryService;
    private final ConnectorOnlineStatusMapper connectorOnlineStatusMapper;

    public ConnectorDetailPageResult connectorDetailPage(DSLContext dsl, ConnectorDetailPageQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        var connectorDbRow = connectorDetailQueryService.queryConnectorDetailPage(dsl, query.getConnectorEndpoint());
        var connector = buildConnectorDetailPageEntry(connectorDbRow);

        var result = new ConnectorDetailPageResult();
        result.setCreatedAt(connector.getCreatedAt());
        result.setEndpoint(connector.getEndpoint());
        result.setParticipantId(connector.getParticipantId());
        result.setLastRefreshAttemptAt(connector.getLastRefreshAttemptAt());
        result.setLastSuccessfulRefreshAt(connector.getLastSuccessfulRefreshAt());
        result.setNumContractOffers(connector.getNumContractOffers());
        result.setOnlineStatus(connector.getOnlineStatus());
        result.setConnectorCrawlingTimeAvg(connector.getConnectorCrawlingTimeAvg());
        return result;
    }

    private ConnectorDetailPageResult buildConnectorDetailPageEntry(ConnectorDetailsRs connector) {
        var dto = new ConnectorDetailPageResult();
        dto.setParticipantId(connector.getParticipantId());
        dto.setEndpoint(connector.getEndpoint());
        dto.setCreatedAt(connector.getCreatedAt());
        dto.setLastRefreshAttemptAt(connector.getLastRefreshAttemptAt());
        dto.setLastSuccessfulRefreshAt(connector.getLastSuccessfulRefreshAt());
        dto.setOnlineStatus(connectorOnlineStatusMapper.getOnlineStatus(connector.getOnlineStatus()));
        dto.setNumContractOffers(connector.getNumDataOffers());
        dto.setConnectorCrawlingTimeAvg(connector.getConnectorCrawlingTimeAvg());
        return dto;
    }
}
