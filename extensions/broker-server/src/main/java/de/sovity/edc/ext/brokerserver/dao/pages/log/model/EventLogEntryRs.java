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

package de.sovity.edc.ext.brokerserver.dao.pages.log.model;

import de.sovity.edc.ext.brokerserver.api.model.EventLogEventStatus;
import de.sovity.edc.ext.brokerserver.api.model.EventLogEventType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventLogEntryRs {
    String eventLogId;
    OffsetDateTime createdAt;
    String userMessage;
    EventLogEventType event;
    EventLogEventStatus eventStatus;
    String connectorEndpoint;
    String assetId;
    String errorStack;
    Long durationInMs;
}


