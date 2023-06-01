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

import de.sovity.edc.ext.brokerserver.BrokerServerExtension;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOffer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.configuration.Config;

import java.util.Collection;

@RequiredArgsConstructor
public class DataOfferFetcher {
    private final ContractOfferFetcher contractOfferFetcher;
    private final DataOfferBuilder dataOfferBuilder;
    private final Config config;
    private final Monitor monitor;

    /**
     * Fetches {@link ContractOffer}s and de-duplicates them into {@link FetchedDataOffer}s.
     *
     * @param connectorEndpoint connector endpoint
     * @return updated connector db row
     */
    @SneakyThrows
    public Collection<FetchedDataOffer> fetch(String connectorEndpoint) {
        // Contract Offers contain assets multiple times, with different policies
        var contractOffers = contractOfferFetcher.fetch(connectorEndpoint);

        // Limit the number of contract offers per connector
        var contractOfferLimit = config.getInteger(BrokerServerExtension.MAX_CONTRACT_OFFERS_PER_CONNECTOR, -1);
        if (contractOfferLimit != -1 && contractOffers.size() > contractOfferLimit) {
            contractOffers = contractOffers.stream().limit(contractOfferLimit).toList();
            monitor.info("Connector " + connectorEndpoint + " has more than " + contractOfferLimit + " contract offers. " +
                    +contractOffers.size() + " contract offers were fetched. " +
                    "Only the first " + contractOfferLimit + " contract offers will be processed.");
        }

        // Data Offers represent unique assets
        return dataOfferBuilder.deduplicateContractOffers(contractOffers);
    }


}
