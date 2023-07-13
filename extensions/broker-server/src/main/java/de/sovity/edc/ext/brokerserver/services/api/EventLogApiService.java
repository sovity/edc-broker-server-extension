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
import de.sovity.edc.ext.brokerserver.dao.pages.log.EventLogPageQueryService;
import de.sovity.edc.ext.brokerserver.dao.pages.log.model.EventLogEntryRs;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class EventLogApiService {
    private final EventLogPageQueryService eventLogPageQueryService;

    public EventLogPageResult eventLogPage(DSLContext dsl, EventLogPageQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        var eventLogRs = eventLogPageQueryService.queryEventLogPage(
            dsl, query.getSearchQuery());
        var result = new EventLogPageResult();
        result.setEventLogEntries(buildEventLogs(eventLogRs.getEventLogs()));
        return result;
    }

    private List<EventLogEntry> buildEventLogs(List<EventLogEntryRs> eventLogEntries) {
        return eventLogEntries.stream()
            .map(this::buildEventLogEntry)
            .toList();
    }

    private EventLogEntry buildEventLogEntry(EventLogEntryRs eventLog) {
        var dto = new EventLogEntry();
        dto.setId(eventLog.getEventLogId());
        dto.setUserMessage(eventLog.getUserMessage());
        dto.setEvent(eventLog.getEvent());
        dto.setEventStatus(eventLog.getEventStatus());
        dto.setConnectorEndpoint(eventLog.getConnectorEndpoint());
        dto.setAssetId(eventLog.getAssetId());
        dto.setErrorStack(eventLog.getErrorStack());
        dto.setCreatedAt(eventLog.getCreatedAt());
        return dto;
    }
}
