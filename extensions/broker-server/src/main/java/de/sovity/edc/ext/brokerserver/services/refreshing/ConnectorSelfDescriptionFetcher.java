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

package de.sovity.edc.ext.brokerserver.services.refreshing;

import de.sovity.edc.ext.brokerserver.dao.models.ConnectorOnlineStatus;
import de.sovity.edc.ext.brokerserver.dao.models.ConnectorRecord;
import de.sovity.edc.ext.brokerserver.sender.message.DescriptionRequestMessage;
import lombok.RequiredArgsConstructor;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;

/**
 * Fetch Connector Metadata.
 */
@RequiredArgsConstructor
public class ConnectorSelfDescriptionFetcher {
    private static final String CONTEXT_SD_REQUEST = "SelfDescriptionRequest";

    private final RemoteMessageDispatcherRegistry dispatcherRegistry;

    /**
     * Fetches Connector metadata and returns an updated {@link ConnectorRecord} if connector online.
     *
     * @param connector existing / stubbed connector db row
     * @return updated connector db row
     */
    public ConnectorRecord updateConnector(ConnectorRecord connector) {
        try {
            var connectorEndpoint = connector.getEndpoint();
            var connectorSelfDescription = fetchConnectorSelfDescription(connectorEndpoint);

            handleConnectorOnline(connector, connectorSelfDescription);
        } catch (Exception e) {
            handleConnectorOffline(connector);
        }

        return connector;
    }

    private String fetchConnectorSelfDescription(String connectorEndpoint)  {
        try {
            var connectorEndpointUrl = new URL(connectorEndpoint);
            var descriptionRequestMessage = new DescriptionRequestMessage(connectorEndpointUrl);
            var descriptionResponseCompletableFuture = dispatcherRegistry.send(String.class, descriptionRequestMessage, () -> CONTEXT_SD_REQUEST);

            return descriptionResponseCompletableFuture.get();
        } catch (MalformedURLException e) {
            throw new EdcException("Invalid connector-endpoint URL", e);
        } catch (Exception e) {
            throw new EdcException("Failed to fetch connector self-description", e);
        }
    }

    private void handleConnectorOffline(ConnectorRecord connector) {
        connector.setLastUpdate(OffsetDateTime.now());
        connector.setOnlineStatus(ConnectorOnlineStatus.OFFLINE);
        connector.setOfflineSince(OffsetDateTime.now());
    }

    private void handleConnectorOnline(ConnectorRecord connector, String connectorSelfDescription) {
        connector.setDescription(connectorSelfDescription);
        connector.setOnlineStatus(ConnectorOnlineStatus.ONLINE);
        connector.setLastUpdate(OffsetDateTime.now());
    }
}
