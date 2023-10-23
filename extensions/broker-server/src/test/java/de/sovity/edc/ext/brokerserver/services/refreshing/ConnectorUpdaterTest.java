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

package de.sovity.edc.ext.brokerserver.services.refreshing;

import de.sovity.edc.client.EdcClient;
import de.sovity.edc.client.gen.model.ContractDefinitionRequest;
import de.sovity.edc.client.gen.model.PolicyDefinitionCreateRequest;
import de.sovity.edc.client.gen.model.UiAssetCreateRequest;
import de.sovity.edc.client.gen.model.UiCriterion;
import de.sovity.edc.client.gen.model.UiCriterionLiteral;
import de.sovity.edc.client.gen.model.UiCriterionLiteralType;
import de.sovity.edc.client.gen.model.UiCriterionOperator;
import de.sovity.edc.client.gen.model.UiPolicyCreateRequest;
import de.sovity.edc.ext.brokerserver.BrokerServerExtensionContext;
import de.sovity.edc.ext.brokerserver.TestUtils;
import de.sovity.edc.ext.brokerserver.client.BrokerServerClient;
import de.sovity.edc.ext.brokerserver.client.gen.model.CatalogPageQuery;
import de.sovity.edc.ext.brokerserver.db.TestDatabase;
import de.sovity.edc.ext.brokerserver.db.TestDatabaseFactory;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import de.sovity.edc.utils.jsonld.vocab.Prop;
import org.eclipse.edc.connector.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.spi.asset.AssetService;
import org.eclipse.edc.junit.annotations.ApiTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.eclipse.edc.spi.asset.AssetSelectorExpression;
import org.eclipse.edc.spi.query.Criterion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static de.sovity.edc.ext.brokerserver.TestUtils.createConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
@ExtendWith(EdcExtension.class)
class ConnectorUpdaterTest {

    @RegisterExtension
    private static final TestDatabase TEST_DATABASE = TestDatabaseFactory.getTestDatabase();

    @RegisterExtension
    static EdcExtension consumerEdcContext = new EdcExtension();

    private EdcClient providerClient;

    private BrokerServerClient brokerServerClient;

    @BeforeEach
    void setUp(EdcExtension extension) {
        extension.setConfiguration(createConfiguration(TEST_DATABASE, Map.of()));

        providerClient = EdcClient.builder()
            .managementApiUrl(TestUtils.MANAGEMENT_ENDPOINT)
            .managementApiKey(TestUtils.MANAGEMENT_API_KEY)
            .build();

        brokerServerClient = BrokerServerClient.builder()
            .managementApiUrl(TestUtils.MANAGEMENT_ENDPOINT)
            .managementApiKey(TestUtils.MANAGEMENT_API_KEY)
            .build();
    }

    @Test
    void testConnectorUpdate() {
        TEST_DATABASE.testTransaction(dsl -> {
            // arrange
            var connectorUpdater = BrokerServerExtensionContext.instance.connectorUpdater();
            var connectorCreator = BrokerServerExtensionContext.instance.connectorCreator();
            String connectorEndpoint = TestUtils.PROTOCOL_ENDPOINT;

            var policyId = createAlwaysTruePolicyDefinition();
            var assetId = createAsset();
            createContractDefinition(policyId, assetId);
            connectorCreator.addConnector(dsl, connectorEndpoint);

            // act
            connectorUpdater.updateConnector(connectorEndpoint);

            // assert
            var catalog = brokerServerClient.brokerServerApi().catalogPage(new CatalogPageQuery());
            // TODO: hasSize1
            var dataOffer = catalog.getDataOffers().get(0);
            // TODO: hasSize1
            var contractOffer = dataOffer.getContractOffers().get(0);
            var asset = dataOffer.getAsset(); // alle felder
            var policy = contractOffer.getContractPolicy(); // alle felder
            // Example at: UiApiWrapperTest
        });
    }

    private String createAlwaysTruePolicyDefinition() {
        return providerClient.uiApi().createPolicyDefinition(PolicyDefinitionCreateRequest.builder()
            .policyDefinitionId("policy-1")
            .policy(UiPolicyCreateRequest.builder()
                .constraints(List.of())
                .build())
            .build()).getId();
    }

    public void createContractDefinition(String policyId, String assetId) {
        providerClient.uiApi().createContractDefinition(ContractDefinitionRequest.builder()
            .contractDefinitionId("cd-1")
            .accessPolicyId(policyId)
            .contractPolicyId(policyId)
            .assetSelector(List.of(UiCriterion.builder()
                .operandLeft(Prop.Edc.ID)
                .operator(UiCriterionOperator.EQ)
                .operandRight(UiCriterionLiteral.builder()
                    .type(UiCriterionLiteralType.VALUE)
                    .value(assetId)
                    .build())
                .build()))
            .build());
    }

    private String createAsset() {
        return providerClient.uiApi().createAsset(UiAssetCreateRequest.builder()
            .id("asset-1")
            .title("AssetName")
            .description("AssetDescription")
            .licenseUrl("https://license-url")
            .version("1.0.0")
            .language("en")
            .mediaType("application/json")
            .dataCategory("dataCategory")
            .dataSubcategory("dataSubcategory")
            .dataModel("dataModel")
            .geoReferenceMethod("geoReferenceMethod")
            .transportMode("transportMode")
            .keywords(List.of("keyword1", "keyword2"))
            .publisherHomepage("publisherHomepage")
            .dataAddressProperties(Map.of(
                Prop.Edc.TYPE, "HttpData",
                Prop.Edc.METHOD, "GET",
                Prop.Edc.BASE_URL, "http://some.url"
            ))
            .additionalProperties(Map.of("http://unknown/a", "x"))
            .additionalJsonProperties(Map.of("http://unknown/b", "{\"http://unknown/c\":\"y\"}"))
            .privateProperties(Map.of("http://unknown/a-private", "x-private"))
            .privateJsonProperties(Map.of("http://unknown/b-private", "{\"http://unknown/c-private\":\"y-private\"}"))
            .build()).getId();
    }
}
