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

package de.sovity.edc.ext.brokerserver.services.schedules;

import de.sovity.edc.ext.brokerserver.dao.ConnectorQueries;
import de.sovity.edc.ext.brokerserver.dao.utils.PostgresqlUtils;
import de.sovity.edc.ext.brokerserver.db.DslContextFactory;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.services.logging.BrokerEventLogger;
import lombok.RequiredArgsConstructor;
import org.eclipse.edc.spi.system.configuration.Config;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@RequiredArgsConstructor
public class DeadConnectorRemoval implements Job {
    private final Config config;
    private final DslContextFactory dslContextFactory;
    private final ConnectorQueries connectorQueries;
    private final BrokerEventLogger brokerEventLogger;

    @Override
    public void execute(JobExecutionContext context) {
        dslContextFactory.transaction(
                dsl -> {
                var deleteOfflineConnectorsAfter = config.getInteger("DELETE_OFFLINE_CONNECTORS_AFTER", 5);
                var toDelete = connectorQueries.findAllConnectorsForDeletion(dsl, deleteOfflineConnectorsAfter);

                // delete in batches, child entities first.
                dsl.deleteFrom(Tables.DATA_OFFER_CONTRACT_OFFER).where(PostgresqlUtils.in(Tables.DATA_OFFER_CONTRACT_OFFER.CONNECTOR_ENDPOINT, toDelete)).execute();
                dsl.deleteFrom(Tables.DATA_OFFER).where(PostgresqlUtils.in(Tables.DATA_OFFER.CONNECTOR_ENDPOINT, toDelete)).execute();
                dsl.deleteFrom(Tables.CONNECTOR).where(PostgresqlUtils.in(Tables.CONNECTOR.ENDPOINT, toDelete)).execute();

                // add log messages
                brokerEventLogger.addDeletedDueToInactivityMessages(dsl, toDelete);
            }
        );
    }
}
