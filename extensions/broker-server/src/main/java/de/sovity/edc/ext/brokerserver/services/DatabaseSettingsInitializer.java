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
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import lombok.RequiredArgsConstructor;
import org.eclipse.edc.spi.system.configuration.Config;
import org.jooq.DSLContext;

@RequiredArgsConstructor
public class DatabaseSettingsInitializer {
    private final Config config;

    private DSLContext dslContext;

    public void initializeSettingsInDatabase(DSLContext dsl) {
        this.dslContext = dsl;

        dsl.insertInto(Tables.BROKER_SERVER_SETTINGS)
                .values(BrokerServerExtension.KNOWN_CONNECTORS, config.getString(BrokerServerExtension.KNOWN_CONNECTORS, ""))
                .values(BrokerServerExtension.CRON_ONLINE_CONNECTOR_REFRESH, config.getString(BrokerServerExtension.CRON_ONLINE_CONNECTOR_REFRESH, ""))
                .values(BrokerServerExtension.CRON_OFFLINE_CONNECTOR_REFRESH, config.getString(BrokerServerExtension.CRON_OFFLINE_CONNECTOR_REFRESH, ""))
                .values(BrokerServerExtension.CRON_DEAD_CONNECTOR_REFRESH, config.getString(BrokerServerExtension.CRON_DEAD_CONNECTOR_REFRESH, ""))
                .values(BrokerServerExtension.NUM_THREADS, config.getString(BrokerServerExtension.NUM_THREADS, ""))
                .values(BrokerServerExtension.HIDE_OFFLINE_DATA_OFFERS_AFTER, config.getString(BrokerServerExtension.HIDE_OFFLINE_DATA_OFFERS_AFTER, ""))
                .values(BrokerServerExtension.MAX_DATA_OFFERS_PER_CONNECTOR, config.getString(BrokerServerExtension.MAX_DATA_OFFERS_PER_CONNECTOR, ""))
                .values(BrokerServerExtension.MAX_CONTRACT_OFFERS_PER_DATA_OFFER, config.getString(BrokerServerExtension.MAX_CONTRACT_OFFERS_PER_DATA_OFFER, ""))
                .values(BrokerServerExtension.CATALOG_PAGE_PAGE_SIZE, config.getString(BrokerServerExtension.CATALOG_PAGE_PAGE_SIZE, ""))
                .values(BrokerServerExtension.DEFAULT_CONNECTOR_DATASPACE, config.getString(BrokerServerExtension.DEFAULT_CONNECTOR_DATASPACE, ""))
                .values(BrokerServerExtension.KNOWN_DATASPACE_CONNECTORS, config.getString(BrokerServerExtension.KNOWN_DATASPACE_CONNECTORS, ""))
                .values(BrokerServerExtension.KILL_OFFLINE_CONNECTORS_AFTER, config.getString(BrokerServerExtension.KILL_OFFLINE_CONNECTORS_AFTER, ""))
                .values(BrokerServerExtension.SCHEDULED_KILL_OFFLINE_CONNECTORS, config.getString(BrokerServerExtension.SCHEDULED_KILL_OFFLINE_CONNECTORS, ""))
                .execute();
    }

    public String readSettingFromDatabase(String name) {
        return dslContext.select(Tables.BROKER_SERVER_SETTINGS.VALUE)
                .from(Tables.BROKER_SERVER_SETTINGS)
                .where(Tables.BROKER_SERVER_SETTINGS.NAME.eq(name))
                .fetchOne(Tables.BROKER_SERVER_SETTINGS.VALUE);
    }
}
