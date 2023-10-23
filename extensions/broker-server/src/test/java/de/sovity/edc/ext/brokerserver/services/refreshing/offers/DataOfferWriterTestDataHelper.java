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

package de.sovity.edc.ext.brokerserver.services.refreshing.offers;

import de.sovity.edc.ext.brokerserver.dao.AssetProperty;
import de.sovity.edc.ext.brokerserver.dao.ConnectorQueries;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.DataOfferContractOfferRecord;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.DataOfferRecord;
import de.sovity.edc.ext.brokerserver.services.ConnectorCreator;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOffer;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedContractOffer;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.JSONB;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.sovity.edc.ext.brokerserver.services.refreshing.offers.DataOfferWriterTestDataModels.Co;
import static de.sovity.edc.ext.brokerserver.services.refreshing.offers.DataOfferWriterTestDataModels.Do;

class DataOfferWriterTestDataHelper {
    String connectorEndpoint = "https://example.com/ids/data";
    OffsetDateTime old = OffsetDateTime.now().withNano(0).withSecond(0).withMinute(0).withHour(0).minusDays(100);
    List<DataOfferContractOfferRecord> existingContractOffers = new ArrayList<>();
    List<DataOfferRecord> existingDataOffers = new ArrayList<>();
    List<FetchedDataOffer> fetchedDataOffers = new ArrayList<>();


    /**
     * Adds fetched data offer
     *
     * @param dataOffer fetched data offer
     */
    public void fetched(Do dataOffer) {
        Validate.notEmpty(dataOffer.getContractOffers());
        fetchedDataOffers.add(dummyFetchedDataOffer(dataOffer));
    }


    /**
     * Adds data offer directly to DB.
     *
     * @param dataOffer data offer
     */
    public void existing(Do dataOffer) {
        Validate.notEmpty(dataOffer.getContractOffers());
        existingDataOffers.add(dummyDataOffer(dataOffer));
        dataOffer.getContractOffers().stream()
            .map(contractOffer -> dummyContractOffer(dataOffer, contractOffer))
            .forEach(existingContractOffers::add);
    }

    public void initialize(DSLContext dsl) {
        var connectorQueries = new ConnectorQueries();
        var connectorCreator = new ConnectorCreator(connectorQueries);
        connectorCreator.addConnector(dsl, connectorEndpoint);
        dsl.batchInsert(existingDataOffers).execute();
        dsl.batchInsert(existingContractOffers).execute();
    }

    private DataOfferContractOfferRecord dummyContractOffer(Do dataOffer, Co contractOffer) {
        var contractOfferRecord = new DataOfferContractOfferRecord();
        contractOfferRecord.setConnectorEndpoint(connectorEndpoint);
        contractOfferRecord.setAssetId(dataOffer.getAssetId());
        contractOfferRecord.setContractOfferId(contractOffer.getId());
        contractOfferRecord.setPolicy(JSONB.valueOf(dummyPolicyJson(contractOffer.getPolicyValue())));
        contractOfferRecord.setCreatedAt(old);
        contractOfferRecord.setUpdatedAt(old);
        return contractOfferRecord;
    }

    private DataOfferRecord dummyDataOffer(Do dataOffer) {
        var assetName = Optional.of(dataOffer.getAssetName()).orElse(dataOffer.getAssetId());

        var dataOfferRecord = new DataOfferRecord();
        dataOfferRecord.setConnectorEndpoint(connectorEndpoint);
        dataOfferRecord.setAssetId(dataOffer.getAssetId());
        dataOfferRecord.setAssetName(assetName);
        dataOfferRecord.setAssetProperties(JSONB.valueOf(dummyAssetJson(dataOffer)));
        dataOfferRecord.setCreatedAt(old);
        dataOfferRecord.setUpdatedAt(old);
        return dataOfferRecord;
    }

    private FetchedDataOffer dummyFetchedDataOffer(Do dataOffer) {
        var fetchedDataOffer = new FetchedDataOffer();
        fetchedDataOffer.setAssetId(dataOffer.getAssetId());
        fetchedDataOffer.setAssetTitle(dataOffer.getAssetName());
        fetchedDataOffer.setAssetPropertiesJsonLd(dummyAssetJson(dataOffer));

        var contractOffersMapped = dataOffer.getContractOffers().stream().map(this::dummyFetchedContractOffer).collect(Collectors.toList());
        fetchedDataOffer.setContractOffers(contractOffersMapped);

        return fetchedDataOffer;
    }

    public String dummyAssetJson(Do dataOffer) {
        return "{\"%s\": \"%s\", \"%s\": \"%s\"}".formatted(
            AssetProperty.ASSET_ID, dataOffer.getAssetId(),
            AssetProperty.ASSET_NAME, dataOffer.getAssetName()
        );
    }

    public String dummyPolicyJson(String policyValue) {
        return "{\"%s\": \"%s\"}".formatted(
            "SomePolicyField", policyValue
        );
    }

    @NotNull
    private FetchedContractOffer dummyFetchedContractOffer(Co it) {
        var contractOffer = new FetchedContractOffer();
        contractOffer.setContractOfferId(it.getId());
        contractOffer.setPolicyJson(dummyPolicyJson(it.getPolicyValue()));
        return contractOffer;
    }
}
