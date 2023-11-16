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

package de.sovity.edc.ext.brokerserver.services.refreshing.offers;

import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedCatalog;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedContractOffer;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOffer;
import de.sovity.edc.ext.wrapper.api.common.mappers.AssetMapper;
import de.sovity.edc.utils.JsonUtils;
import de.sovity.edc.utils.catalog.model.DspCatalog;
import de.sovity.edc.utils.catalog.model.DspContractOffer;
import de.sovity.edc.utils.catalog.model.DspDataOffer;
import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public class FetchedCatalogBuilder {
    private final AssetMapper assetMapper;

    public FetchedCatalog buildFetchedCatalog(DspCatalog catalog) {
        var fetchedDataOffers = catalog.getDataOffers().stream()
                .map(this::buildFetchedDataOffer)
                .toList();

        var fetchedCatalog = new FetchedCatalog();
        fetchedCatalog.setParticipantId(catalog.getParticipantId());
        fetchedCatalog.setDataOffers(fetchedDataOffers);

        return fetchedCatalog;
    }

    @NotNull
    private FetchedDataOffer buildFetchedDataOffer(DspDataOffer dspDataOffer) {
        var assetJsonLd = assetMapper.buildAssetJsonLdFromDatasetProperties(dspDataOffer.getAssetPropertiesJsonLd());

        var fetchedDataOffer = new FetchedDataOffer();
        setAssetMetadata(fetchedDataOffer, assetJsonLd);
        fetchedDataOffer.setContractOffers(buildFetchedContractOffers(dspDataOffer.getContractOffers()));
        return fetchedDataOffer;
    }

    @NotNull
    private List<FetchedContractOffer> buildFetchedContractOffers(List<DspContractOffer> offers) {
        return offers.stream()
            .map(this::buildFetchedContractOffer)
            .toList();
    }

    @NotNull
    private FetchedContractOffer buildFetchedContractOffer(DspContractOffer offer) {
        var contractOffer = new FetchedContractOffer();
        contractOffer.setContractOfferId(offer.getContractOfferId());
        contractOffer.setPolicyJson(JsonUtils.toJson(offer.getPolicyJsonLd()));
        return contractOffer;
    }

    /**
     * This method was extract so tests could re-use the logic of assetJsonLd -&gt; fetchedDataOffer -&gt; dataOfferRecord
     *
     * @param fetchedDataOffer fetchedDataOffer
     * @param assetJsonLd assetJsonLd
     */
    public void setAssetMetadata(FetchedDataOffer fetchedDataOffer, JsonObject assetJsonLd) {
        var uiAsset = assetMapper.buildUiAsset(assetJsonLd, "http://irrelevant-here", "irrelevant-here");
        fetchedDataOffer.setAssetId(uiAsset.getAssetId());
        fetchedDataOffer.setAssetTitle(uiAsset.getTitle());
        fetchedDataOffer.setAssetJsonLd(JsonUtils.toJson(assetJsonLd));

        // Most of these fields are extracted so our DB does not need to
        // semantically interpret JSON-LD when searching and filtering
        fetchedDataOffer.setDescription(uiAsset.getDescription());
        fetchedDataOffer.setCuratorOrganizationName(uiAsset.getCreatorOrganizationName());

        fetchedDataOffer.setDataCategory(uiAsset.getDataCategory());
        fetchedDataOffer.setDataSubcategory(uiAsset.getDataSubcategory());
        fetchedDataOffer.setDataModel(uiAsset.getDataModel());
        fetchedDataOffer.setGeoReferenceMethod(uiAsset.getGeoReferenceMethod());
        fetchedDataOffer.setKeywords(uiAsset.getKeywords());
    }
}
