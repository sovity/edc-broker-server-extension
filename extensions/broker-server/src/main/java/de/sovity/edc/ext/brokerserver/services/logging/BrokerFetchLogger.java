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

package de.sovity.edc.ext.brokerserver.services.logging;

import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.BrokerFetchLogRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Updates a single connector.
 */
@RequiredArgsConstructor
public class BrokerFetchLogger {

    public void logConnectorCrawledExecutionTime(DSLContext dsl, String connectorEndpoint, String logMessage, long start, long end) {
        var logEntry = connectorUpdateEntry(dsl, connectorEndpoint);
        logEntry.setConnectorEndpoint(connectorEndpoint);
        logEntry.setLogMessage(logMessage);
        logEntry.setStartTime(OffsetDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneOffset.UTC));
        logEntry.setEndTime(OffsetDateTime.ofInstant(Instant.ofEpochMilli(end), ZoneOffset.UTC));
        logEntry.insert();
    }

    private BrokerFetchLogRecord connectorUpdateEntry(DSLContext dsl, String connectorEndpoint) {
        var logEntry = dsl.newRecord(Tables.BROKER_FETCH_LOG);
        logEntry.setConnectorEndpoint(connectorEndpoint);
        logEntry.setCreatedAt(OffsetDateTime.now());
        return logEntry;
    }
}
