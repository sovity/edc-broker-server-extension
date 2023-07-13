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

package de.sovity.edc.ext.brokerserver.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "An Event Log Entry")
public class EventLogEntry {
    @Schema(description = "Event Log ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "User Message", example = "Successful refresh.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userMessage;

    @Schema(description = "Event", example = "Refresh", requiredMode = Schema.RequiredMode.REQUIRED)
    private String event;

    @Schema(description = "Event Status", example = "SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventStatus;

    @Schema(description = "Connector Endpoint", example = "https://my-test.connector/control/ids/data", requiredMode = Schema.RequiredMode.REQUIRED)
    private String connectorEndpoint;

    @Schema(description = "Asset ID", example = "https://my-test.connector/control/ids/data/1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String assetId;

    @Schema(description = "Error Stack", example = "Null pointer exception", requiredMode = Schema.RequiredMode.REQUIRED)
    private String errorStack;

    @Schema(description = "Duration in milliseconds", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationInMs;

    @Schema(description = "Log creation time", requiredMode = Schema.RequiredMode.REQUIRED)
    private OffsetDateTime createdAt;
}
