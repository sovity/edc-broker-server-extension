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

package de.sovity.edc.ext.brokerserver.services.api;

import de.sovity.edc.ext.brokerserver.TestPolicy;
import de.sovity.edc.ext.brokerserver.client.gen.model.AuthorityPortalOrganizationMetadata;
import de.sovity.edc.ext.brokerserver.client.gen.model.AuthorityPortalOrganizationMetadataRequest;
import de.sovity.edc.ext.brokerserver.client.gen.model.ConnectorPageQuery;
import de.sovity.edc.ext.brokerserver.client.gen.model.DataOfferDetailPageQuery;
import de.sovity.edc.ext.brokerserver.db.TestDatabase;
import de.sovity.edc.ext.brokerserver.db.TestDatabaseFactory;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorContractOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorDataOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static de.sovity.edc.ext.brokerserver.TestAsset.getAssetJsonLd;
import static de.sovity.edc.ext.brokerserver.TestAsset.setDataOfferAssetMetadata;
import static de.sovity.edc.ext.brokerserver.TestUtils.ADMIN_API_KEY;
import static de.sovity.edc.ext.brokerserver.TestUtils.brokerServerClient;
import static de.sovity.edc.ext.brokerserver.TestUtils.createConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
@ExtendWith(EdcExtension.class)
class AuthorityPortalOrganizationMetadataApiTest {

    @RegisterExtension
    private static final TestDatabase TEST_DATABASE = TestDatabaseFactory.getTestDatabase();

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(createConfiguration(TEST_DATABASE, Map.of()));
    }

    @Test
    void testSetOrganizationMetadataExists() {
        TEST_DATABASE.testTransaction(dsl -> {
            // arrange
            var now = OffsetDateTime.now().withNano(0);

            createConnector(dsl, now, 1, "MDSL1234AA");
            createDataOffer(dsl, now, 1, 1);

            // act
            var orgMetadata = new AuthorityPortalOrganizationMetadata();
            orgMetadata.setMdsId("MDSL1234AA");
            orgMetadata.setName("Test Org");
            var orgMetadataRequest = new AuthorityPortalOrganizationMetadataRequest();
            orgMetadataRequest.setOrganizations(List.of(orgMetadata));

            brokerServerClient().brokerServerApi().setOrganizationMetadata(
                ADMIN_API_KEY,
                orgMetadataRequest
            );

            // assert
            var connectorPage = brokerServerClient().brokerServerApi().connectorPage(new ConnectorPageQuery());
            var connector = connectorPage.getConnectors().get(0);
            assertThat(connector.getOrganizationName()).isEqualTo("Test Org");

            var dataOfferDetailPage = brokerServerClient().brokerServerApi().dataOfferDetailPage(new DataOfferDetailPageQuery(getEndpoint(1), "my-asset-1"));
            var asset = dataOfferDetailPage.getAsset();
            assertThat(asset.getCreatorOrganizationName()).isEqualTo("Test Org");
        });
    }

    @Test
    void testSetOrganizationMetadataNotExists() {
        TEST_DATABASE.testTransaction(dsl -> {
            // arrange
            var now = OffsetDateTime.now().withNano(0);

            createConnector(dsl, now, 1, "MDSL1234AA");
            createDataOffer(dsl, now, 1, 1);

            // act
            var orgMetadata = new AuthorityPortalOrganizationMetadata();
            orgMetadata.setMdsId("MDSL4321ZZ");
            orgMetadata.setName("Test Org");
            var orgMetadataRequest = new AuthorityPortalOrganizationMetadataRequest();
            orgMetadataRequest.setOrganizations(List.of(orgMetadata));

            brokerServerClient().brokerServerApi().setOrganizationMetadata(
                ADMIN_API_KEY,
                orgMetadataRequest
            );

            // assert
            var connectorPage = brokerServerClient().brokerServerApi().connectorPage(new ConnectorPageQuery());
            var connector = connectorPage.getConnectors().get(0);
            assertThat(connector.getOrganizationName()).isEqualTo("Unknown");

            var dataOfferDetailPage = brokerServerClient().brokerServerApi().dataOfferDetailPage(new DataOfferDetailPageQuery(getEndpoint(1), "my-asset-1"));
            var asset = dataOfferDetailPage.getAsset();
            assertThat(asset.getCreatorOrganizationName()).isEqualTo("Unknown");
        });
    }

    private void createConnector(DSLContext dsl, OffsetDateTime now, int iConnector, String mdsId) {
        var connector = dsl.newRecord(Tables.CONNECTOR);
        connector.setParticipantId("my-connector");
        connector.setMdsId(mdsId);
        connector.setEndpoint(getEndpoint(iConnector));
        connector.setOnlineStatus(ConnectorOnlineStatus.ONLINE);
        connector.setCreatedAt(now.minusDays(1));
        connector.setLastRefreshAttemptAt(now);
        connector.setLastSuccessfulRefreshAt(now);
        connector.setDataOffersExceeded(ConnectorDataOffersExceeded.OK);
        connector.setContractOffersExceeded(ConnectorContractOffersExceeded.OK);
        connector.insert();
    }

    private String getEndpoint(int iConnector) {
        return "https://connector-%d".formatted(iConnector);
    }

    private void createDataOffer(DSLContext dsl, OffsetDateTime now, int iConnector, int iDataOffer) {
        var connectorEndpoint = getEndpoint(iConnector);
        var assetJsonLd = getAssetJsonLd("my-asset-%d".formatted(iDataOffer));

        var dataOffer = dsl.newRecord(Tables.DATA_OFFER);
        setDataOfferAssetMetadata(dataOffer, assetJsonLd, "my-participant-id");
        dataOffer.setConnectorEndpoint(connectorEndpoint);
        dataOffer.setCreatedAt(now.minusDays(5));
        dataOffer.setUpdatedAt(now);
        dataOffer.insert();

        var contractOffer = dsl.newRecord(Tables.CONTRACT_OFFER);
        contractOffer.setContractOfferId("my-contract-offer-1");
        contractOffer.setConnectorEndpoint(connectorEndpoint);
        contractOffer.setAssetId(dataOffer.getAssetId());
        contractOffer.setCreatedAt(now.minusDays(5));
        contractOffer.setUpdatedAt(now);
        contractOffer.setPolicy(TestPolicy.createAfterYesterdayPolicyJson());
        contractOffer.insert();
    }
}
