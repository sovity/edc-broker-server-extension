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

package de.sovity.edc.ext.brokerserver.services.refreshing;

import de.sovity.edc.ext.brokerserver.BrokerServerExtension;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorContractOffersExceeded;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.ConnectorRecord;
import de.sovity.edc.ext.brokerserver.services.logging.BrokerEventLogger;
import de.sovity.edc.ext.brokerserver.services.logging.ConnectorChangeTracker;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.DataOfferWriter;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.model.FetchedDataOffer;
import lombok.RequiredArgsConstructor;
import org.eclipse.edc.spi.system.configuration.Config;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ConnectorUpdateSuccessWriter {
    private final BrokerEventLogger brokerEventLogger;
    private final DataOfferWriter dataOfferWriter;
    private final Config config;

    public void handleConnectorOnline(
            DSLContext dsl,
            ConnectorRecord connector,
            Collection<FetchedDataOffer> dataOffers
    ) {
        var now = OffsetDateTime.now();

        // Limit data offers if necessary
        var limitedDataOffers = getDataOffersWithLimit(dataOffers, connector);

        // Log Status Change and set status to online if necessary
        if (connector.getOnlineStatus() == ConnectorOnlineStatus.OFFLINE || connector.getLastRefreshAttemptAt() == null) {
            brokerEventLogger.logConnectorUpdateStatusChange(dsl, connector.getEndpoint(), ConnectorOnlineStatus.ONLINE);
            connector.setOnlineStatus(ConnectorOnlineStatus.ONLINE);
        }

        // Track changes for final log message
        var changes = new ConnectorChangeTracker();
        connector.setLastSuccessfulRefreshAt(now);
        connector.setLastRefreshAttemptAt(now);
        connector.update();

        // Log Event if changes are present
        if (!changes.isEmpty()) {
            brokerEventLogger.logConnectorUpdateSuccess(dsl, connector.getEndpoint(), changes);
        }

        // Update data offers
        dataOfferWriter.updateDataOffers(dsl, connector.getEndpoint(), limitedDataOffers, changes);
    }

    @NotNull
    private List<FetchedDataOffer> getDataOffersWithLimit(Collection<FetchedDataOffer> dataOffers, ConnectorRecord connector) {
        var maxDataOffersPerConnector = config.getInteger(BrokerServerExtension.MAX_DATA_OFFERS_PER_CONNECTOR, -1);
        var offerList = dataOffers.stream().toList();

        var dataOffersCount = offerList.size();

        if (maxDataOffersPerConnector != -1 && dataOffersCount > maxDataOffersPerConnector) {
            if (connector.getContractOffersExceeded() == ConnectorContractOffersExceeded.OK) {
                brokerEventLogger.logConnectorUpdateDataOfferLimitExceeded(dataOffersCount, maxDataOffersPerConnector, connector.getEndpoint());
                connector.setContractOffersExceeded(ConnectorContractOffersExceeded.EXCEEDED);
            }
            offerList = offerList.subList(0, maxDataOffersPerConnector);
        } else if (connector.getContractOffersExceeded() == ConnectorContractOffersExceeded.EXCEEDED) {
            brokerEventLogger.logConnectorUpdateDataOfferLimitOk(dataOffersCount, maxDataOffersPerConnector, connector.getEndpoint());
            connector.setContractOffersExceeded(ConnectorContractOffersExceeded.OK);
        }

        return offerList;
    }
}
