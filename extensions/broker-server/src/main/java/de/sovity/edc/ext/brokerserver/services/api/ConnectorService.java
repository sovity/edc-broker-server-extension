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
import de.sovity.edc.ext.brokerserver.api.model.ConnectorEndpoint;
import de.sovity.edc.ext.brokerserver.api.model.ConnectorListEntry;
import de.sovity.edc.ext.brokerserver.api.model.ConnectorPageQuery;
import de.sovity.edc.ext.brokerserver.api.model.ConnectorPageResult;
import de.sovity.edc.ext.brokerserver.api.model.ConnectorPageSortingItem;
import de.sovity.edc.ext.brokerserver.api.model.ConnectorPageSortingType;
import de.sovity.edc.ext.brokerserver.dao.pages.connector.ConnectorPageQueryService;
import de.sovity.edc.ext.brokerserver.dao.pages.connector.model.ConnectorRs;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static de.sovity.edc.ext.brokerserver.db.jooq.Tables.CONNECTOR;

@RequiredArgsConstructor
public class ConnectorService {

    public void addConnector(DSLContext dsl, String connectorEndpoint) {
        var connectorDbRow = dsl.selectFrom(CONNECTOR)
                .where(CONNECTOR.CONNECTOR_ID.eq(connectorEndpoint))
                .fetchOne();

        if (connectorDbRow != null) {
            return;
        }

        dsl.insertInto(CONNECTOR)
                .set(CONNECTOR.CONNECTOR_ID, connectorEndpoint)
                .set(CONNECTOR.ENDPOINT, connectorEndpoint)
                .set(CONNECTOR.ONLINE_STATUS, ConnectorOnlineStatus.OFFLINE)
                .set(CONNECTOR.CREATED_AT, OffsetDateTime.now())
                .execute();
    }
}
