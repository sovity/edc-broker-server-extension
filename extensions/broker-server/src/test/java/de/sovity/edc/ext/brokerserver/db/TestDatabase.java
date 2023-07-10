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

import org.eclipse.edc.spi.system.configuration.Config;
import org.jooq.DSLContext;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;

import javax.sql.DataSource;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public interface TestDatabase extends BeforeAllCallback, AfterAllCallback {
    String getJdbcUrl();

    String getJdbcUser();

    String getJdbcPassword();

    /**
     * New {@link DslContextFactory} from the test database's credentials
     *
     * @return {@link DslContextFactory}
     */
    default DslContextFactory getDslContextFactory() {
        var dataSource = getDataSource();
        return new DslContextFactory(dataSource);
    }

    /**
     * Returns a {@link DataSource} to the test database
     *
     * @return {@link DataSource}
     */
    default DataSource getDataSource() {
        var config = mock(Config.class);
        when(config.getString(PostgresFlywayExtension.JDBC_URL, "")).thenReturn(getJdbcUrl());
        when(config.getString(PostgresFlywayExtension.JDBC_USER , "")).thenReturn(getJdbcUser());
        when(config.getString(PostgresFlywayExtension.JDBC_PASSWORD, "")).thenReturn(getJdbcPassword());
        var dataSourceFactory = new DataSourceFactory(config);
        return dataSourceFactory.newDataSource();
    }

    /**
     * Runs given code within a test transaction.
     * <br>
     * Globally hijacks all {@link DslContextFactory}s to use this test transaction.
     *
     * @param code code to run within the test transaction
     */
    default void testTransaction(Consumer<DSLContext> code) {
        try {
            getDslContextFactory().transaction(dsl -> DslContextFactoryHijacker.withParentDslContext(dsl, () -> {
                code.accept(dsl);
                throw new TestDatabaseCancelTransactionException();
            }));
        } catch (TestDatabaseCancelTransactionException e) {
            // Ignore
        }
    }
}
