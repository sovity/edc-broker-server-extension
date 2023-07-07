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
 *       sovity GmbH - initial implementation
 *
 */

package de.sovity.edc.ext.brokerserver.db;

import lombok.RequiredArgsConstructor;
import org.eclipse.edc.sql.datasource.ConnectionFactoryDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

@RequiredArgsConstructor
public class DataSourceFactory {
    private final HikariCPDataSource hikariCpDataSource;

    public DataSource newDataSource() {
        return new ConnectionFactoryDataSource(this::newConnection);
    }

    private Connection newConnection() {
        return hikariCpDataSource.getConnection();
    }
}
