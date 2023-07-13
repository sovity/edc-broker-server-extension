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

package de.sovity.edc.ext.brokerserver.services.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.sovity.edc.ext.brokerserver.client.gen.model.EventLogPageQuery;
import de.sovity.edc.ext.brokerserver.db.TestDatabase;
import de.sovity.edc.ext.brokerserver.db.TestDatabaseFactory;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.*;
import lombok.SneakyThrows;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.eclipse.edc.policy.model.Policy;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.OffsetDateTime;
import java.util.Map;

import static de.sovity.edc.ext.brokerserver.TestUtils.*;
import static groovy.json.JsonOutput.toJson;
import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
@ExtendWith(EdcExtension.class)
class EventLogApiTest {
    @RegisterExtension
    private static final TestDatabase TEST_DATABASE = TestDatabaseFactory.getTestDatabase();

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(createConfiguration(TEST_DATABASE, Map.of()));
    }

    @Test
    void testQueryEventLogs() {
        TEST_DATABASE.testTransaction(dsl -> {
            var today = OffsetDateTime.now().withNano(0);

            createEventLogEntry(dsl, today, "http://my-event-log/ids/log");

            var result = brokerServerClient().brokerServerApi().eventLogPage(new EventLogPageQuery());
            assertThat(result.getEventLogEntries()).hasSize(1);

            var eventLog = result.getEventLogEntries().get(0);
            assertThat(eventLog.getId()).isEqualTo("http://my-event-log/ids/log");
            assertThat(eventLog.getCreatedAt()).isEqualTo(today.minusDays(1));
        });
    }
    private void createEventLogEntry(DSLContext dsl, OffsetDateTime today, String connectorEndpoint) {
        var eventLog = dsl.newRecord(Tables.BROKER_EVENT_LOG);
        eventLog.setCreatedAt(today.minusDays(1));
        eventLog.setEvent(BrokerEventType.CONNECTOR_UPDATED);
        eventLog.setEventStatus(BrokerEventStatus.OK);
        eventLog.setConnectorEndpoint(connectorEndpoint);
        eventLog.insert();

    }
}
