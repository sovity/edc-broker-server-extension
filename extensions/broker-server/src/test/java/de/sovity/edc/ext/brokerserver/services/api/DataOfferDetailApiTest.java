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

import de.sovity.edc.ext.brokerserver.client.gen.model.DataOfferDetailPageQuery;
import de.sovity.edc.ext.brokerserver.client.gen.model.DataOfferDetailPageResult;
import de.sovity.edc.ext.brokerserver.db.TestDatabase;
import de.sovity.edc.ext.brokerserver.db.TestDatabaseFactory;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorContractOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorDataOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import de.sovity.edc.ext.wrapper.api.common.mappers.utils.AssetJsonLdUtils;
import de.sovity.edc.utils.JsonUtils;
import de.sovity.edc.utils.jsonld.vocab.Prop;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.OffsetDateTime;
import java.util.Map;

import static de.sovity.edc.ext.brokerserver.AssertionUtils.assertEqualUsingJson;
import static de.sovity.edc.ext.brokerserver.TestPolicy.createAfterYesterdayConstraint;
import static de.sovity.edc.ext.brokerserver.TestPolicy.createAfterYesterdayPolicyJson;
import static de.sovity.edc.ext.brokerserver.TestUtils.brokerServerClient;
import static de.sovity.edc.ext.brokerserver.TestUtils.createConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
@ExtendWith(EdcExtension.class)
class DataOfferDetailApiTest {

    @RegisterExtension
    private static final TestDatabase TEST_DATABASE = TestDatabaseFactory.getTestDatabase();

    static AssetJsonLdUtils assetJsonLdUtils = new AssetJsonLdUtils();

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(createConfiguration(TEST_DATABASE, Map.of(
        )));
    }

    @Test
    void testQueryDataOfferDetails() {
        TEST_DATABASE.testTransaction(dsl -> {
            var today = OffsetDateTime.now().withNano(0);

            createConnector(dsl, today, "http://my-connector2/dsp");
            createDataOffer(dsl, today, "http://my-connector2/dsp", Json.createObjectBuilder()
                .add(Prop.ID, "my-asset-2")
                .add(Prop.Edc.PROPERTIES, Json.createObjectBuilder()
                    .add(Prop.Mds.DATA_CATEGORY, "my-category-2")
                    .add(Prop.Dcterms.TITLE, "My Asset 2")
                )
                .build()
            );

            createConnector(dsl, today, "http://my-connector/dsp");
            createDataOffer(dsl, today, "http://my-connector/dsp", Json.createObjectBuilder()
                .add(Prop.ID, "my-asset-1")
                .add(Prop.Edc.PROPERTIES, Json.createObjectBuilder()
                    .add(Prop.Mds.DATA_CATEGORY, "my-category")
                    .add(Prop.Dcterms.TITLE, "My Asset 1")
                )
                .build()
            );

            //create view for dataoffer
            createDataOfferView(dsl, today, "http://my-connector/dsp", Json.createObjectBuilder()
                .add(Prop.ID, "my-asset-1")
                .add(Prop.Edc.PROPERTIES, Json.createObjectBuilder()
                    .add(Prop.Mds.DATA_CATEGORY, "my-category")
                    .add(Prop.Dcterms.TITLE, "My Asset 1")
                )
                .build()
            );
            createDataOfferView(dsl, today, "http://my-connector/dsp", Json.createObjectBuilder()
                .add(Prop.ID, "my-asset-1")
                .add(Prop.Edc.PROPERTIES, Json.createObjectBuilder()
                    .add(Prop.Mds.DATA_CATEGORY, "my-category")
                    .add(Prop.Dcterms.TITLE, "My Asset 1")
                )
                .build()
            );

            var actual = brokerServerClient().brokerServerApi().dataOfferDetailPage(new DataOfferDetailPageQuery("http://my-connector/dsp", "my-asset-1"));
            assertThat(actual.getAssetId()).isEqualTo("my-asset-1");
            assertThat(actual.getConnectorEndpoint()).isEqualTo("http://my-connector/dsp");
            assertThat(actual.getConnectorOfflineSinceOrLastUpdatedAt()).isEqualTo(today);
            assertThat(actual.getConnectorOnlineStatus()).isEqualTo(DataOfferDetailPageResult.ConnectorOnlineStatusEnum.ONLINE);
            assertThat(actual.getCreatedAt()).isEqualTo(today.minusDays(5));
            assertThat(actual.getAsset().getAssetId()).isEqualTo("my-asset-1");
            assertThat(actual.getAsset().getDataCategory()).isEqualTo("my-category");
            assertThat(actual.getAsset().getTitle()).isEqualTo("My Asset 1");
            assertThat(actual.getUpdatedAt()).isEqualTo(today);
            assertThat(actual.getContractOffers()).hasSize(1);
            var contractOffer = actual.getContractOffers().get(0);
            assertThat(contractOffer.getContractOfferId()).isEqualTo("my-contract-offer-1");
            assertEqualUsingJson(contractOffer.getContractPolicy().getConstraints().get(0), createAfterYesterdayConstraint());
            assertThat(contractOffer.getCreatedAt()).isEqualTo(today.minusDays(5));
            assertThat(contractOffer.getUpdatedAt()).isEqualTo(today);
            assertThat(actual.getViewCount()).isEqualTo(2);
        });
    }

    private void createConnector(DSLContext dsl, OffsetDateTime today, String connectorEndpoint) {
        var connector = dsl.newRecord(Tables.CONNECTOR);
        connector.setConnectorId("http://my-connector");
        connector.setEndpoint(connectorEndpoint);
        connector.setOnlineStatus(ConnectorOnlineStatus.ONLINE);
        connector.setCreatedAt(today.minusDays(1));
        connector.setLastRefreshAttemptAt(today);
        connector.setLastSuccessfulRefreshAt(today);
        connector.setDataOffersExceeded(ConnectorDataOffersExceeded.OK);
        connector.setContractOffersExceeded(ConnectorContractOffersExceeded.OK);
        connector.insert();
    }

    private void createDataOffer(DSLContext dsl, OffsetDateTime today, String connectorEndpoint, JsonObject assetJsonLd) {
        var dataOffer = dsl.newRecord(Tables.DATA_OFFER);
        dataOffer.setAssetId(assetJsonLdUtils.getId(assetJsonLd));
        dataOffer.setAssetName(assetJsonLdUtils.getTitle(assetJsonLd));
        dataOffer.setAssetProperties(JSONB.jsonb(JsonUtils.toJson(assetJsonLd)));
        dataOffer.setConnectorEndpoint(connectorEndpoint);
        dataOffer.setCreatedAt(today.minusDays(5));
        dataOffer.setUpdatedAt(today);
        dataOffer.insert();

        var contractOffer = dsl.newRecord(Tables.DATA_OFFER_CONTRACT_OFFER);
        contractOffer.setContractOfferId("my-contract-offer-1");
        contractOffer.setConnectorEndpoint(connectorEndpoint);
        contractOffer.setAssetId(assetJsonLdUtils.getId(assetJsonLd));
        contractOffer.setCreatedAt(today.minusDays(5));
        contractOffer.setUpdatedAt(today);
        contractOffer.setPolicy(createAfterYesterdayPolicyJson());
        contractOffer.insert();
    }

    private static void createDataOfferView(DSLContext dsl, OffsetDateTime date, String connectorEndpoint, JsonObject assetPropertiesJson) {
        var view = dsl.newRecord(Tables.DATA_OFFER_VIEW_COUNT);
        view.setAssetId(assetJsonLdUtils.getId(assetPropertiesJson));
        view.setConnectorEndpoint(connectorEndpoint);
        view.setDate(date);
        view.insert();
    }
}
