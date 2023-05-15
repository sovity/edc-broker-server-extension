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

package de.sovity.edc.ext.brokerserver.dao.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

/**
 * Connector Database Row that can be inserted or updated.
 * <p>
 * Represents metadata for another connector in the dataspace.
 */
@Getter
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ConnectorRecord {
    final String id;
    final String idsId;
    final String title;
    final String description;
    final String endpoint;
    final OffsetDateTime lastUpdate;
    final OffsetDateTime offlineSince;
    final OffsetDateTime createdAt;
    final ConnectorOnlineStatus onlineStatus;
    @Setter
    boolean forceDeleted;
}

