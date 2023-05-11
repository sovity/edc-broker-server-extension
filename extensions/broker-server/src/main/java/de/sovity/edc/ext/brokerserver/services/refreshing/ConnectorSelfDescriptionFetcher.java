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

package de.sovity.edc.ext.brokerserver.services.refreshing;

import de.sovity.edc.ext.brokerserver.dao.models.ConnectorRecord;

/**
 * Fetch Connector Metadata.
 */
public class ConnectorSelfDescriptionFetcher {

    /**
     * Fetches Connector metadata and returns an updated {@link ConnectorRecord}
     *
     * @param connector existing / stubbed connector db row
     * @return updated connector db row
     */
    public ConnectorRecord updateConnector(ConnectorRecord connector) {
        // TODO implement
        throw new IllegalArgumentException("Not yet implemented");
    }
}
