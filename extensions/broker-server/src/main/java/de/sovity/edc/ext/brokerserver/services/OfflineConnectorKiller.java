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

import de.sovity.edc.ext.brokerserver.dao.ConnectorQueries;
import de.sovity.edc.ext.brokerserver.dao.utils.PostgresqlUtils;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import de.sovity.edc.ext.brokerserver.services.config.BrokerServerSettings;
import de.sovity.edc.ext.brokerserver.services.logging.BrokerEventLogger;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

@RequiredArgsConstructor
public class OfflineConnectorKiller {
    private final BrokerServerSettings brokerServerSettings;
    private final ConnectorQueries connectorQueries;
    private final BrokerEventLogger brokerEventLogger;

    public void killIfOfflineTooLong(DSLContext dsl) {
        var killOfflineConnectorsAfter = brokerServerSettings.getDeleteOfflineConnectorsAfter();
        var toKill = connectorQueries.findAllConnectorsForDeletion(dsl, killOfflineConnectorsAfter);

        // delete data offers in batches, child entities first.
        dsl.deleteFrom(Tables.DATA_OFFER_CONTRACT_OFFER).where(PostgresqlUtils.in(Tables.DATA_OFFER_CONTRACT_OFFER.CONNECTOR_ENDPOINT, toKill)).execute();
        dsl.deleteFrom(Tables.DATA_OFFER).where(PostgresqlUtils.in(Tables.DATA_OFFER.CONNECTOR_ENDPOINT, toKill)).execute();

        // set connector to status dead
        dsl.update(Tables.CONNECTOR).set(Tables.CONNECTOR.ONLINE_STATUS, ConnectorOnlineStatus.DEAD).where(PostgresqlUtils.in(Tables.CONNECTOR.ENDPOINT, toKill)).execute();

        // add log messages
        brokerEventLogger.addKilledDueToOfflineTooLongMessages(dsl, toKill);
    }
}
