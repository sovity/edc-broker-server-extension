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

import de.sovity.edc.ext.brokerserver.BrokerServerExtension;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorContractOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorDataOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.ConnectorRecord;
import de.sovity.edc.ext.brokerserver.services.logging.BrokerEventLogger;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOffer;
import lombok.RequiredArgsConstructor;
import org.eclipse.edc.spi.system.configuration.Config;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class DataOfferLimitsEnforcer {
    private final Config config;
    private final BrokerEventLogger brokerEventLogger;

    private List<FetchedDataOffer> offerList;

    public record DataOfferLimitsEnforced(
            Collection<FetchedDataOffer> abbreviatedDataOffers,
            boolean dataOfferLimitsExceeded,
            boolean contractOfferLimitsExceeded
    ) {
    }

    public DataOfferLimitsEnforced enforceLimits(Collection<FetchedDataOffer> dataOffers) {
        // Get limits from config
        var maxDataOffers = config.getInteger(BrokerServerExtension.MAX_DATA_OFFERS_PER_CONNECTOR, -1);
        var maxContractOffers = config.getInteger(BrokerServerExtension.MAX_CONTRACT_OFFERS_PER_CONNECTOR, -1);

        // No limits set
        if (maxDataOffers == -1 && maxContractOffers == -1) {
            return new DataOfferLimitsEnforced(dataOffers, false, false);
        }

        offerList = dataOffers.stream().toList();

        // Validate if limits exceeded
        var dataOfferLimitsExceeded = isDataOfferLimitsExceeded(dataOffers, maxDataOffers);
        var contractOfferLimitsExceeded = isContractOfferLimitsExceeded(dataOffers, maxContractOffers);

        // Create new list with limited offers
        return new DataOfferLimitsEnforced(dataOffers, dataOfferLimitsExceeded, contractOfferLimitsExceeded);
    }

    public void logEnforcedLimitsIfChanged(ConnectorRecord connector, DataOfferLimitsEnforced enforcedLimits) {
        // DataOffer
        if (enforcedLimits.dataOfferLimitsExceeded() && connector.getDataOffersExceeded() == ConnectorDataOffersExceeded.OK) {
            brokerEventLogger.logConnectorUpdateDataOfferLimitExceeded(offerList.size(), connector.getEndpoint());
            connector.setDataOffersExceeded(ConnectorDataOffersExceeded.EXCEEDED);
        } else if (!enforcedLimits.dataOfferLimitsExceeded() && connector.getDataOffersExceeded() == ConnectorDataOffersExceeded.EXCEEDED) {
            brokerEventLogger.logConnectorUpdateDataOfferLimitOk(offerList.size(), connector.getEndpoint());
            connector.setDataOffersExceeded(ConnectorDataOffersExceeded.OK);
        }

        // ContractOffer
        if (enforcedLimits.contractOfferLimitsExceeded() && connector.getContractOffersExceeded() == ConnectorContractOffersExceeded.OK) {
            brokerEventLogger.logConnectorUpdateContractOfferLimitExceeded(offerList.size(), connector.getEndpoint());
            connector.setContractOffersExceeded(ConnectorContractOffersExceeded.EXCEEDED);
        } else if (!enforcedLimits.contractOfferLimitsExceeded() && connector.getContractOffersExceeded() == ConnectorContractOffersExceeded.EXCEEDED) {
            brokerEventLogger.logConnectorUpdateContractOfferLimitOk(offerList.size(), connector.getEndpoint());
            connector.setContractOffersExceeded(ConnectorContractOffersExceeded.OK);
        }
    }

    private boolean isDataOfferLimitsExceeded(Collection<FetchedDataOffer> dataOffers, Integer maxDataOffers) {
        if (maxDataOffers != -1 && dataOffers.size() > maxDataOffers) {
            offerList = offerList.subList(0, maxDataOffers - 1);
            return true;
        }

        return false;
    }

    private boolean isContractOfferLimitsExceeded(Collection<FetchedDataOffer> dataOffers, Integer maxContractOffers) {
        var contractOfferLimitsExceeded = false;
        for (var dataOffer : dataOffers) {
            var contractOffers = dataOffer.getContractOffers();
            if (maxContractOffers != -1 && contractOffers.size() > maxContractOffers) {
                dataOffer.setContractOffers(contractOffers.subList(0, maxContractOffers - 1));
                contractOfferLimitsExceeded = true;
            }
        }

        return contractOfferLimitsExceeded;
    }
}
