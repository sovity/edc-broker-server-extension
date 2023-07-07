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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.system.configuration.Config;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariCPDataSource {
    private HikariConfig hikariConfig = new HikariConfig();
    private HikariDataSource hikariDataSource;

    public HikariCPDataSource(Config config) {
        hikariConfig.setJdbcUrl(config.getString(PostgresFlywayExtension.JDBC_URL, ""));
        hikariConfig.setUsername(config.getString(PostgresFlywayExtension.JDBC_USER, ""));
        hikariConfig.setPassword(config.getString(PostgresFlywayExtension.JDBC_PASSWORD, ""));
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setPoolName("edc-broker-server");
        hikariConfig.setMaxLifetime(50000);
        hikariConfig.setConnectionTimeout(30000);

        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() {
        try {
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new EdcException("Could not create db connection", e);
        }
    }
}
