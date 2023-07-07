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

import de.sovity.edc.ext.brokerserver.api.model.*;
import de.sovity.edc.ext.brokerserver.dao.pages.log.LogPageQueryService;
import de.sovity.edc.ext.brokerserver.dao.pages.log.model.LogRs;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.util.Objects;

@RequiredArgsConstructor
public class LogApiService {
    private final LogPageQueryService logPageQueryService;


    public ConnectorEventLogPageResult connectorLogPage(DSLContext dsl, ConnectorEventLogPageQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        var eventLogDbRow = logPageQueryService.queryConnectorLogPage(dsl, query.getLogId());
        var eventLog = buildEventLogEntry(eventLogDbRow);

        var result = new ConnectorEventLogPageResult();
        result.setCreatedAt(eventLog.getCreatedAt());
        result.setUserMessage(eventLog.getUserMessage());
        result.setEvent(eventLog.getEvent());
        result.setEventStatus(eventLog.getEventStatus());
        result.setConnectorEndpoint(eventLog.getConnectorEndpoint());
        result.setAssetId(eventLog.getAssetId());
        result.setErrorStack(eventLog.getErrorStack());
        result.setDurationInMs(Long.valueOf(eventLog.getDurationInMs()));
        return result;
    }

    private LogEntry buildEventLogEntry(LogRs eventLog) {
        var dto = new LogEntry();

        dto.setId(eventLog.getLogId());
        dto.setUserMessage(eventLog.getUserMessage());
        dto.setEvent(eventLog.getEvent());
        dto.setEventStatus(eventLog.getEventStatus());
        dto.setConnectorEndpoint(eventLog.getConnectorEndpoint());
        dto.setAssetId(eventLog.getAssetId());
        dto.setErrorStack(eventLog.getErrorStack());
        dto.setDurationInMs(Math.toIntExact(eventLog.getDurationInMs()));
        dto.setCreatedAt(eventLog.getCreatedAt());

        return dto;
    }
}
