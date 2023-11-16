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
import de.sovity.edc.ext.wrapper.api.common.mappers.utils.AssetJsonLdUtils;
import de.sovity.edc.utils.JsonUtils;
import de.sovity.edc.utils.catalog.model.DspCatalog;
import de.sovity.edc.utils.catalog.model.DspContractOffer;
import de.sovity.edc.utils.catalog.model.DspDataOffer;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public class FetchedCatalogBuilder {
    private final AssetMapper assetMapper;
    private final AssetJsonLdUtils assetJsonLdUtils;

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
        fetchedDataOffer.setAssetId(assetJsonLdUtils.getId(assetJsonLd));
        fetchedDataOffer.setAssetTitle(assetJsonLdUtils.getTitle(assetJsonLd));
        fetchedDataOffer.setAssetJsonLd(JsonUtils.toJson(assetJsonLd));
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
}
