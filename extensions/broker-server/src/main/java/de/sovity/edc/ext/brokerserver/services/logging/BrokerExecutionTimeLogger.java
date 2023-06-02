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
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ErrorStatus;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.MeasurementType;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.BrokerExecutionTimeRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.time.OffsetDateTime;

/**
 * Updates a single connector.
 */
@RequiredArgsConstructor
public class BrokerExecutionTimeLogger {
    public void logSuccess(DSLContext dsl, String connectorEndpoint, long executionTime) {
        logExecutionTime(dsl, connectorEndpoint, executionTime, ErrorStatus.OK);
    }

    public void logError(DSLContext dsl, String connectorEndpoint, long executionTime) {
        logExecutionTime(dsl, connectorEndpoint, executionTime, ErrorStatus.ERROR);
    }

    private void logExecutionTime(DSLContext dsl, String connectorEndpoint, long executionTime, ErrorStatus errorStatus) {
        var logEntry = connectorUpdateEntry(dsl, connectorEndpoint);
        logEntry.setConnectorEndpoint(connectorEndpoint);
        logEntry.setDurationInMs(executionTime);
        logEntry.setType(MeasurementType.CONNECTOR_REFRESH);
        logEntry.setErrorStatus(errorStatus);
        logEntry.insert();
    }

    private BrokerExecutionTimeRecord connectorUpdateEntry(DSLContext dsl, String connectorEndpoint) {
        var logEntry = dsl.newRecord(Tables.BROKER_EXECUTION_TIME);
        logEntry.setConnectorEndpoint(connectorEndpoint);
        logEntry.setCreatedAt(OffsetDateTime.now());
        return logEntry;
    }
}
