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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.edc.connector.spi.catalog.CatalogService;
import org.eclipse.edc.spi.query.QuerySpec;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class DataOfferFetcher {
    private final CatalogService catalogService;

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
            var json = fetchCatalogJson(connectorEndpoint);
            var dataSets = json.getJSONArray("dcat:dataset");

            for (int i = 0; i < dataSets.length(); i++) {
                var dataSet = dataSets.getJSONObject(i);

                var assetId = dataSet.getString("edc:id");
                var name = dataSet.getString("edc:name");
                var policyJson = dataSet.getJSONObject("odrl:hasPolicy").toString();

                dataSet.remove("odrl:hasPolicy");

                var fetchedDataOfferContractOffer = new FetchedDataOfferContractOffer();
                fetchedDataOfferContractOffer.setContractOfferId(assetId);
                fetchedDataOfferContractOffer.setPolicyJson(policyJson);

                var contractOffers = new ArrayList<FetchedDataOfferContractOffer>();
                contractOffers.add(fetchedDataOfferContractOffer);

                var dataOffer = new FetchedDataOffer();
                dataOffer.setAssetId(assetId);
                dataOffer.setAssetName(name);
                dataOffer.setAssetPropertiesJson(dataSet.toString());
                dataOffer.setContractOffers(contractOffers);

                dataOfferList.add(dataOffer);
            }

            return dataOfferList;

        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectorUnreachableException("Failed to fetch connector contract offers", e);
        }
    }

    private JSONObject fetchCatalogJson(String connectorEndpoint) throws InterruptedException, ExecutionException {
        var resultContent = catalogService.requestCatalog(connectorEndpoint,
            "dataspace-protocol-http", QuerySpec.max()).get().getContent();
        return new JSONObject(new String(resultContent, StandardCharsets.UTF_8));
    }
}
