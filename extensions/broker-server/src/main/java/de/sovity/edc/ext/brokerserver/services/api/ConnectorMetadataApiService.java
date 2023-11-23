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

import de.sovity.edc.ext.brokerserver.api.model.AuthorityPortalConnectorInfo;
import de.sovity.edc.ext.brokerserver.dao.utils.PostgresqlUtils;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.time.OffsetDateTime;
import java.util.List;

import static org.jooq.impl.DSL.*;

@RequiredArgsConstructor
public class ConnectorMetadataApiService {
    private final ConnectorOnlineStatusMapper connectorOnlineStatusMapper;

    public List<AuthorityPortalConnectorInfo> getMetadataByEndpoints(DSLContext dsl, List<String> endpoints) {

        return getConnectorMetadataRs(dsl, endpoints).stream()
            .map(it -> new AuthorityPortalConnectorInfo(
                it.connectorEndpoint,
                it.participantId,
                it.dataOfferCount,
                connectorOnlineStatusMapper.getOnlineStatus(it.onlineStatus),
                it.onlineStatusRefreshedAt
            ))
            .toList();
    }

    // TODO: Move below to a new service "ConnectorQueryService"

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ConnectorMetadataRs {
        String connectorEndpoint;
        String participantId;
        ConnectorOnlineStatus onlineStatus;
        OffsetDateTime onlineStatusRefreshedAt;
        Integer dataOfferCount;

    }

    @NotNull
    private List<ConnectorMetadataRs> getConnectorMetadataRs(DSLContext dsl, List<String> endpoints) {
        var c = Tables.CONNECTOR;

        return dsl.select(
                c.ENDPOINT.as("connectorEndpoint"),
                c.PARTICIPANT_ID.as("participantId"),
                c.ONLINE_STATUS.as("onlineStatus"),
                c.LAST_SUCCESSFUL_REFRESH_AT.as("onlineStatusRefreshedAt"),
                getDataOfferCount(c.ENDPOINT).as("dataOfferCount")
            )
            .from(c)
            .where(PostgresqlUtils.in(c.ENDPOINT, endpoints))
            .fetchInto(ConnectorMetadataRs.class);
    }

    @NotNull
    private Field<Integer> getDataOfferCount(Field<String> connectorEndpoint) {
        var d = Tables.DATA_OFFER;

        return select(coalesce(count().cast(Integer.class), DSL.value(0)))
            .from(d)
            .where(d.CONNECTOR_ENDPOINT.eq(connectorEndpoint))
            .asField();
    }
}
