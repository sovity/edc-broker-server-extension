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
import de.sovity.edc.client.gen.model.CatalogPageQuery;
import de.sovity.edc.ext.brokerserver.db.TestDatabase;
import de.sovity.edc.ext.brokerserver.db.TestDatabaseFactory;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.eclipse.edc.policy.model.Policy;
import org.jooq.JSONB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.OffsetDateTime;
import java.util.Map;

import static de.sovity.edc.client.gen.model.DataOfferListEntry.ConnectorOnlineStatusEnum.ONLINE;
import static de.sovity.edc.ext.brokerserver.TestUtils.createConfiguration;
import static de.sovity.edc.ext.brokerserver.TestUtils.edcClient;
import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
@ExtendWith(EdcExtension.class)
class CatalogApiTest {

    @RegisterExtension
    private static final TestDatabase TEST_DATABASE = TestDatabaseFactory.getTestDatabase();

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(createConfiguration(TEST_DATABASE, Map.of()));
    }

    @Test
    void testQueryConnectors() {
        TEST_DATABASE.testTransaction(dsl -> {
            // arrange
            var today = OffsetDateTime.now().withNano(0);

            var connector = dsl.newRecord(Tables.CONNECTOR);
            connector.setConnectorId("http://my-connector");
            connector.setEndpoint("http://my-connector/ids/data");
            connector.setOnlineStatus(ConnectorOnlineStatus.ONLINE);
            connector.setCreatedAt(today.minusDays(1));
            connector.setLastRefreshAttemptAt(today);
            connector.setLastSuccessfulRefreshAt(today);
            connector.setForceDeleted(false);
            connector.insert();

            var dataOffer = dsl.newRecord(Tables.DATA_OFFER);
            dataOffer.setAssetId("urn:artifact:my-asset");
            dataOffer.setAssetProperties(JSONB.jsonb("{\"asset:prop:id\": \"urn:artifact:my-asset\", \"asset:prop:name\": \"my-asset\"}"));
            dataOffer.setConnectorEndpoint("http://my-connector/ids/data");
            dataOffer.setCreatedAt(today.minusDays(5));
            dataOffer.setUpdatedAt(today);
            dataOffer.insert();

            var contractOffer = dsl.newRecord(Tables.DATA_OFFER_CONTRACT_OFFER);
            contractOffer.setContractOfferId("my-contract-offer-1");
            contractOffer.setConnectorEndpoint("http://my-connector/ids/data");
            contractOffer.setAssetId("urn:artifact:my-asset");
            contractOffer.setCreatedAt(today.minusDays(5));
            contractOffer.setUpdatedAt(today);
            contractOffer.setPolicy(JSONB.jsonb(policyToJson(dummyPolicy())));
            contractOffer.insert();


            var result = edcClient().brokerServerApi().catalogPage(new CatalogPageQuery());
            assertThat(result.getDataOffers()).hasSize(1);

            var dataOfferResult = result.getDataOffers().get(0);
            assertThat(dataOfferResult.getConnectorEndpoint()).isEqualTo("http://my-connector/ids/data");
            assertThat(dataOfferResult.getConnectorOfflineSinceOrLastUpdatedAt()).isEqualTo(today);
            assertThat(dataOfferResult.getConnectorOnlineStatus()).isEqualTo(ONLINE);
            assertThat(dataOfferResult.getAssetId()).isEqualTo("urn:artifact:my-asset");
            assertThat(dataOfferResult.getProperties()).isEqualTo(Map.of(
                    "asset:prop:id", "urn:artifact:my-asset",
                    "asset:prop:name", "my-asset"
            ));
            assertThat(dataOfferResult.getCreatedAt()).isEqualTo(today.minusDays(5));
            assertThat(toJson(dataOfferResult.getContractOffers().get(0).getContractPolicy().getLegacyPolicy())).isEqualTo(toJson(dummyPolicy()));
        });
    }

    private Policy dummyPolicy() {
        return Policy.Builder.newInstance()
                .assignee("Example Assignee")
                .build();
    }

    private String policyToJson(Policy policy) {
        return toJson(policy);
    }

    private String toJson(Object o) {
        return new ObjectMapper().valueToTree(o).toString();
    }
}
