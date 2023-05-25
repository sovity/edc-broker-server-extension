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
package de.sovity.edc.ext.brokerserver.services.queue;

import de.sovity.edc.ext.brokerserver.dao.queries.ConnectorQueries;
import de.sovity.edc.ext.brokerserver.db.DslContextFactory;
import de.sovity.edc.ext.brokerserver.services.ConnectorQueueEntry;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
public class ConnectorQueueJob implements Job {
    private final DslContextFactory dslContextFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var connectorQueries = new ConnectorQueries();
        var connectorQueue = (ConnectorQueue) context.get("connectorQueue");

        dslContextFactory.transaction(dsl -> {
            var connectorRecords = connectorQueries.findAll(dsl);
            connectorRecords.forEach(connectorRecord -> {
                var connectorQueueEntry = new ConnectorQueueEntry(connectorRecord.getEndpoint(), OffsetDateTime.now(), ConnectorRefreshPriority.SCHEDULED_REFRESH);
                connectorQueue.add(connectorQueueEntry);
            });
        });
    }
}
