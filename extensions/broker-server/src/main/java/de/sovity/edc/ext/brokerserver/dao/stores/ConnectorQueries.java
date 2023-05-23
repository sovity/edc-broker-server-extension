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

package de.sovity.edc.ext.brokerserver.dao.stores;

import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.ConnectorRecord;
import org.jooq.DSLContext;

import java.util.stream.Stream;

public class ConnectorQueries {

    public Stream<ConnectorRecord> findAll(DSLContext dslContext) {
        return dslContext.selectFrom(Tables.CONNECTOR).stream();
    }

    public ConnectorRecord findByEndpoint(DSLContext dsl, String endpoint) {
        var c = Tables.CONNECTOR;
        return dsl.selectFrom(c).where(c.ENDPOINT.eq(endpoint)).fetchOne();
    }
}
