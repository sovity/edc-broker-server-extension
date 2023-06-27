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

package de.sovity.edc.ext.brokerserver.services.schedules;

import de.sovity.edc.ext.brokerserver.BrokerServerExtensionContext;
import de.sovity.edc.ext.brokerserver.TestUtils;
import de.sovity.edc.ext.brokerserver.db.TestDatabase;
import de.sovity.edc.ext.brokerserver.db.TestDatabaseFactory;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorContractOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorDataOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.quartz.JobExecutionContext;

import java.time.OffsetDateTime;
import java.util.Map;

import static de.sovity.edc.ext.brokerserver.TestUtils.createConfiguration;
import static de.sovity.edc.ext.brokerserver.db.jooq.tables.Connector.CONNECTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(EdcExtension.class)
class DeadConnectorRemovalTest {

    @RegisterExtension
    private static final TestDatabase TEST_DATABASE = TestDatabaseFactory.getTestDatabase();

    private final JobExecutionContext context = mock(JobExecutionContext.class);

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(createConfiguration(TEST_DATABASE, Map.of(
                "EDC_BROKER_SERVER_DELETE_OFFLINE_CONNECTORS_AFTER", "P5D"
        )));
    }

    @Test
    void test_deadConnectorRemoval_should_remove() {
        TEST_DATABASE.testTransaction(dsl -> {
            // arrange
            createConnector(dsl, 6);
            var deadConnectorRemoval = BrokerServerExtensionContext.instance.deadConnectorRemoval();

            // act
            deadConnectorRemoval.execute(context);

            // assert
            assertThat(dsl.selectCount().from(CONNECTOR).fetchOne(0, Integer.class)).isZero();
        });
    }

    @Test
    void test_deadConnectorRemoval_should_not_remove() {
        TEST_DATABASE.testTransaction(dsl -> {
            // arrange
            createConnector(dsl, 2);
            var deadConnectorRemoval = BrokerServerExtensionContext.instance.deadConnectorRemoval();

            // act
            deadConnectorRemoval.execute(context);

            // assert
            assertThat(dsl.selectCount().from(CONNECTOR).fetchOne(0, Integer.class)).isNotZero();
        });
    }

    private static void createConnector(DSLContext dsl, int createdDaysAgo) {
        dsl.insertInto(CONNECTOR)
            .set(CONNECTOR.CONNECTOR_ID, "http://example.org")
            .set(CONNECTOR.ENDPOINT, TestUtils.MANAGEMENT_ENDPOINT)
            .set(CONNECTOR.ONLINE_STATUS, ConnectorOnlineStatus.OFFLINE)
            .set(CONNECTOR.LAST_SUCCESSFUL_REFRESH_AT, OffsetDateTime.now().minusDays(createdDaysAgo))
            .set(CONNECTOR.CREATED_AT, OffsetDateTime.now().minusDays(6))
            .set(CONNECTOR.DATA_OFFERS_EXCEEDED, ConnectorDataOffersExceeded.OK)
            .set(CONNECTOR.CONTRACT_OFFERS_EXCEEDED, ConnectorContractOffersExceeded.OK).execute();
    }
}
