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
import lombok.*;

import java.time.OffsetDateTime;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Connector Log Page Data")
public class ConnectorEventLogPageResult {

    @Schema(description = "Timestamp when the event was created", example = "2019-01-01T00:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private OffsetDateTime createdAt;

    @Schema(description = "User message", example = "Connector was successfully updated, and changes were incorporated", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userMessage;

    @Schema(description = "Event", example = "CONNECTOR_UPDATED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String event;

    @Schema(description = "Event Status", example = "OK", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventStatus;

    @Schema(description = "Connector Endpoint", example = "https://my-connector.com/ids/data", requiredMode = Schema.RequiredMode.REQUIRED)
    private String connectorEndpoint;

    @Schema(description = "Asset ID", example = "test-asset-1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String assetId;

    @Schema(description = "Error Stack")
    private String errorStack;

    @Schema(description = "Duration in milliseconds", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long durationInMs;
}


