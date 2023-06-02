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

@RequiredArgsConstructor
public class DataOfferLimitsEnforcer {
    private final Config config;
    private final BrokerEventLogger brokerEventLogger;

    public record DataOfferLimitsEnforced(
            Collection<FetchedDataOffer> abbreviatedDataOffers,
            boolean dataOfferLimitsExceeded,
            boolean contractOfferLimitsExceeded
    ) {
    }

    //TODO: use function to enforce limits
    public DataOfferLimitsEnforced enforceDataOfferAndContractOfferLimits(ConnectorRecord connector, Collection<FetchedDataOffer> dataOffers) {
        // Get limits from config
        var maxDataOffers = config.getInteger(BrokerServerExtension.MAX_DATA_OFFERS_PER_CONNECTOR, -1);
        var maxContractOffers = config.getInteger(BrokerServerExtension.MAX_CONTRACT_OFFERS_PER_CONNECTOR, -1);
        var dataOfferLimitsExceeded = false;
        var contractOfferLimitsExceeded = false;

        // No limits set
        if (maxDataOffers == -1 && maxContractOffers == -1) {
            return new DataOfferLimitsEnforced(dataOffers, false, false);
        }

        var offerList = dataOffers.stream().toList();

        // Check if dataoffer limit exceeded
        if (maxDataOffers != -1 && dataOffers.size() > maxDataOffers) {
            offerList = offerList.subList(0, maxDataOffers - 1);
            dataOfferLimitsExceeded = true;
        }

        // Check if contractoffer limit exceeded and limit dataoffers if necessary
        var maxContractOffersCount = 0;
        for (var dataOffer : dataOffers) {
            var contractOffers = dataOffer.getContractOffers();
            if (contractOffers.size() > maxContractOffersCount) {
                maxContractOffersCount = contractOffers.size();
            }
            if (maxContractOffers != -1 && contractOffers.size() > maxContractOffers) {
                dataOffer.setContractOffers(contractOffers.subList(0, maxContractOffers - 1));
                contractOfferLimitsExceeded = true;
            }
        }

        // Create new list with limited offers
        var limitsEnforced = new DataOfferLimitsEnforced(dataOffers, dataOfferLimitsExceeded, contractOfferLimitsExceeded);

        // Log if limits exceeded (data offer limits)
        if (dataOfferLimitsExceeded && connector.getDataOffersExceeded() == ConnectorDataOffersExceeded.OK) {
            brokerEventLogger.logConnectorUpdateDataOfferLimitExceeded(dataOffers.size(), maxDataOffers, connector.getEndpoint());
            connector.setDataOffersExceeded(ConnectorDataOffersExceeded.EXCEEDED);
        } else if (!dataOfferLimitsExceeded && connector.getDataOffersExceeded() == ConnectorDataOffersExceeded.EXCEEDED) {
            brokerEventLogger.logConnectorUpdateDataOfferLimitOk(dataOffers.size(), maxDataOffers, connector.getEndpoint());
            connector.setDataOffersExceeded(ConnectorDataOffersExceeded.OK);
        }

        // Log if limits exceeded (contract offer limits)
        if (contractOfferLimitsExceeded && connector.getContractOffersExceeded() == ConnectorContractOffersExceeded.OK) {
            brokerEventLogger.logConnectorUpdateContractOfferLimitExceeded(maxContractOffersCount, maxDataOffers, connector.getEndpoint());
            connector.setContractOffersExceeded(ConnectorContractOffersExceeded.EXCEEDED);
        } else if (!contractOfferLimitsExceeded && connector.getContractOffersExceeded() == ConnectorContractOffersExceeded.EXCEEDED) {
            brokerEventLogger.logConnectorUpdateContractOfferLimitOk(maxContractOffersCount, maxDataOffers, connector.getEndpoint());
            connector.setContractOffersExceeded(ConnectorContractOffersExceeded.OK);
        }

        return limitsEnforced;
    }
}
