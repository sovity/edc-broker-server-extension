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

package de.sovity.edc.ext.brokerserver.services.refreshing.offers.writing;

import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.DataOfferRecord;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.fetching.FetchedDataOffer;
import lombok.RequiredArgsConstructor;
import org.jooq.JSONB;

import java.time.OffsetDateTime;
import java.util.Objects;

@RequiredArgsConstructor
public class DataOfferRecordUpdater {
    /**
     * Create a new {@link DataOfferRecord}.
     *
     * @param connectorEndpoint connector endpoint
     * @param fetchedDataOffer  {@link FetchedDataOffer}
     * @return {@link DataOfferRecord}
     */
    public DataOfferRecord newDataOffer(String connectorEndpoint, FetchedDataOffer fetchedDataOffer) {
        var dataOffer = new DataOfferRecord();
        dataOffer.setConnectorEndpoint(connectorEndpoint);
        dataOffer.setAssetId(fetchedDataOffer.getAssetId());
        dataOffer.setCreatedAt(OffsetDateTime.now());
        updateDataOffer(dataOffer, fetchedDataOffer);
        return dataOffer;
    }


    /**
     * Update existing {@link DataOfferRecord}.
     *
     * @param dataOffer        {@link DataOfferRecord}
     * @param fetchedDataOffer {@link FetchedDataOffer}
     * @return whether any fields were updated
     */
    public boolean updateDataOffer(DataOfferRecord dataOffer, FetchedDataOffer fetchedDataOffer) {
        String existingAssetProps = dataOffer.getAssetProperties().data();
        String fetchedAssetProps = fetchedDataOffer.getAssetPropertiesJson();
        if (!Objects.equals(fetchedAssetProps, existingAssetProps)) {
            dataOffer.setAssetProperties(JSONB.jsonb(fetchedAssetProps));
            return true;
        }
        return false;
    }
}
