/*
 *  Copyright (c) 2022 sovity GmbH
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

package de.sovity.edc.ext.brokerserver.fetcher;

import de.sovity.edc.ext.brokerserver.dao.models.ConnectorRecord;
import de.sovity.edc.ext.brokerserver.dao.stores.InMemoryConnectorStore;

import java.time.OffsetDateTime;

public class ConnectorDescriptionFetcher {
    ConnectorRecord withUpdatedSelfDescription(ConnectorRecord connector) {
        var inMemoryConnectorStore = new InMemoryConnectorStore();
        var connectorRecord = inMemoryConnectorStore.findById(connector.getId());
        if (connectorRecord != null) {
            var newConnectorRecord = ConnectorRecord.builder()
                    .id(connectorRecord.getId())
                    .description(connectorRecord.getDescription())
                    .onlineStatus(connectorRecord.getOnlineStatus())
                    .title(connectorRecord.getTitle())
                    .idsId(connectorRecord.getIdsId())
                    .endpoint(connectorRecord.getEndpoint())
                    .lastUpdate(OffsetDateTime.now())
                    .build();

            inMemoryConnectorStore.save(newConnectorRecord);
        }
        return connector;
    }
}
