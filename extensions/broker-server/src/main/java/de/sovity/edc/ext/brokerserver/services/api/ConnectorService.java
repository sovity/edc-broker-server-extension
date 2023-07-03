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

import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorContractOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorDataOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.time.OffsetDateTime;

import static de.sovity.edc.ext.brokerserver.db.jooq.Tables.CONNECTOR;

@RequiredArgsConstructor
public class ConnectorService {

    public void addConnector(DSLContext dsl, String connectorEndpoint) {
        // validate connector doesn't yet exist
        var c = CONNECTOR;
        var trimmedConnectorEndpoint = connectorEndpoint.trim();
        var connectorDbRow = dsl.selectFrom(c)
                .where(c.ENDPOINT.eq(trimmedConnectorEndpoint))
                .fetchOne();

        if (connectorDbRow != null) {
            return;
        }

        // add connector
        dsl.insertInto(CONNECTOR)
                .set(CONNECTOR.CONNECTOR_ID, trimmedConnectorEndpoint)
                .set(CONNECTOR.ENDPOINT, trimmedConnectorEndpoint)
                .set(CONNECTOR.ONLINE_STATUS, ConnectorOnlineStatus.OFFLINE)
                .set(CONNECTOR.CREATED_AT, OffsetDateTime.now())
                .set(CONNECTOR.DATA_OFFERS_EXCEEDED, ConnectorDataOffersExceeded.OK)
                .set(CONNECTOR.CONTRACT_OFFERS_EXCEEDED, ConnectorContractOffersExceeded.OK)
                .execute();
    }
}
