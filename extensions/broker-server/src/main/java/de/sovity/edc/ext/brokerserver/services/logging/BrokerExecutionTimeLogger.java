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
import de.sovity.edc.ext.brokerserver.db.jooq.enums.MeasurementErrorStatus;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.MeasurementType;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.BrokerExecutionTimeMeasurementRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.time.OffsetDateTime;

/**
 * Updates a single connector.
 */
@RequiredArgsConstructor
public class BrokerExecutionTimeLogger {
    public void logSuccess(DSLContext dsl, String connectorEndpoint, long executionTimeInMs) {
        logExecutionTime(dsl, connectorEndpoint, executionTimeInMs, MeasurementErrorStatus.OK);
    }

    public void logError(DSLContext dsl, String connectorEndpoint, long executionTimeInMs) {
        logExecutionTime(dsl, connectorEndpoint, executionTimeInMs, MeasurementErrorStatus.ERROR);
    }

    private void logExecutionTime(DSLContext dsl, String connectorEndpoint, long executionTimeInMs, MeasurementErrorStatus errorStatus) {
        var logEntry = connectorUpdateEntry(dsl, connectorEndpoint);
        logEntry.setConnectorEndpoint(connectorEndpoint);
        logEntry.setDurationInMs(executionTimeInMs);
        logEntry.setType(MeasurementType.CONNECTOR_REFRESH);
        logEntry.setErrorStatus(errorStatus);
        logEntry.insert();
    }

    private BrokerExecutionTimeMeasurementRecord connectorUpdateEntry(DSLContext dsl, String connectorEndpoint) {
        var logEntry = dsl.newRecord(Tables.BROKER_EXECUTION_TIME_MEASUREMENT);
        logEntry.setConnectorEndpoint(connectorEndpoint);
        logEntry.setCreatedAt(OffsetDateTime.now());
        return logEntry;
    }
}
