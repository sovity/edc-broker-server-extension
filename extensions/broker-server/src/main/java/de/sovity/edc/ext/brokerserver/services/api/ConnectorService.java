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
        var connectorDbRow = dsl.selectFrom(c)
                .where(c.ENDPOINT.eq(connectorEndpoint))
                .fetchOne();

        if (connectorDbRow != null) {
            return;
        }

        // add connector
        dsl.insertInto(CONNECTOR)
                .set(CONNECTOR.CONNECTOR_ID, connectorEndpoint)
                .set(CONNECTOR.ENDPOINT, connectorEndpoint)
                .set(CONNECTOR.ONLINE_STATUS, ConnectorOnlineStatus.OFFLINE)
                .set(CONNECTOR.CREATED_AT, OffsetDateTime.now())
                .execute();
    }
}
