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

package de.sovity.edc.ext.brokerserver.services;

import de.sovity.edc.ext.brokerserver.BrokerServerExtension;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.ConnectorRecord;
import de.sovity.edc.ext.brokerserver.services.queue.ConnectorQueue;
import de.sovity.edc.ext.brokerserver.services.queue.ConnectorRefreshPriority;
import de.sovity.edc.ext.brokerserver.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.spi.system.configuration.Config;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class ConnectorCreator {
    private final Config config;
    private final ConnectorQueue connectorQueue;

    public void addKnownConnectorsOnStartup(DSLContext dsl) {
        List<String> connectorEndpoints = getKnownConnectorsConfigValue();
        addConnectors(dsl, connectorEndpoints);
        connectorQueue.addAll(connectorEndpoints, ConnectorRefreshPriority.ADDED_ON_STARTUP);
    }

    public void addConnectors(DSLContext dsl, List<String> connectorEndpoints) {
        var connectorRecords = connectorEndpoints.stream()
                .map(String::trim)
                .map(this::newConnectorRow)
                .toList();
        dsl.batchStore(connectorRecords).execute();
    }

    @NotNull
    private ConnectorRecord newConnectorRow(String endpoint) {
        var connectorId = UrlUtils.getEverythingBeforeThePath(endpoint);

        var connector = new ConnectorRecord();
        connector.setEndpoint(endpoint);
        connector.setConnectorId(connectorId);
        connector.setTitle("Unknown Connector");
        connector.setDescription("Awaiting initial crawling of given connector.");
        connector.setIdsId("");
        connector.setCreatedAt(OffsetDateTime.now());
        connector.setOnlineStatus(ConnectorOnlineStatus.OFFLINE);
        return connector;
    }

    private List<String> getKnownConnectorsConfigValue() {
        String knownConnectorsString = config.getString(BrokerServerExtension.KNOWN_CONNECTORS, "");
        return Arrays.stream(knownConnectorsString.split(",")).map(String::trim).filter(StringUtils::isNotBlank).distinct().toList();
    }
}
