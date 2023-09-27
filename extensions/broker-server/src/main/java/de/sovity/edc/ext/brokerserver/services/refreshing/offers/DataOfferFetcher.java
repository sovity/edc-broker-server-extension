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

import de.sovity.edc.ext.brokerserver.services.refreshing.exceptions.ConnectorUnreachableException;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOffer;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOfferContractOffer;
import de.sovity.edc.utils.catalog.DspCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class DataOfferFetcher {
    private final DspCatalogService dspCatalogService;

    /**
     * Fetches Connector contract offers
     *
     * @param connectorEndpoint connector endpoint
     * @return updated connector db row
     */
    @SneakyThrows
    public List<FetchedDataOffer> fetch(String connectorEndpoint) {
        var dataOfferList = new ArrayList<FetchedDataOffer>();

        try {
            var dspDataOffers = dspCatalogService.fetchDataOffers(connectorEndpoint);

            for (int i = 0; i < dspDataOffers.size(); i++) {
                var dspDataOffer = dspDataOffers.get(i);
                var assertProperties = dspDataOffer.getAssetPropertiesJsonLd();

                var assetId = assertProperties.getString("edc:id");
                var name = assertProperties.getString("edc:name");
                var policyJson = assertProperties.getJsonObject("odrl:hasPolicy").toString();

                var fetchedDataOfferContractOffer = new FetchedDataOfferContractOffer();
                fetchedDataOfferContractOffer.setContractOfferId(assetId);
                fetchedDataOfferContractOffer.setPolicyJson(policyJson);

                var contractOffers = new ArrayList<FetchedDataOfferContractOffer>();
                contractOffers.add(fetchedDataOfferContractOffer);

                var dataOffer = new FetchedDataOffer();
                dataOffer.setAssetId(assetId);
                dataOffer.setAssetName(name);
                dataOffer.setAssetPropertiesJson(assertProperties.toString());
                dataOffer.setContractOffers(contractOffers);

                dataOfferList.add(dataOffer);
            }

            return dataOfferList;
        } catch (Exception e) {
            throw new ConnectorUnreachableException("Failed to fetch connector contract offers", e);
        }
    }
}
