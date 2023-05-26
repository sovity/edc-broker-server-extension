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

import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.DataOfferContractOfferRecord;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.DataOfferRecord;
import de.sovity.edc.ext.brokerserver.services.refreshing.offers.fetching.FetchedDataOfferContractOffer;
import lombok.RequiredArgsConstructor;
import org.jooq.JSONB;

import java.time.OffsetDateTime;
import java.util.Objects;

@RequiredArgsConstructor
public class ContractOfferRecordUpdater {

    public DataOfferContractOfferRecord newContractOffer(DataOfferRecord dataOffer, FetchedDataOfferContractOffer fetchedContractOffer) {
        var contractOffer = new DataOfferContractOfferRecord();
        contractOffer.setConnectorEndpoint(dataOffer.getConnectorEndpoint());
        contractOffer.setAssetId(dataOffer.getAssetId());
        contractOffer.setCreatedAt(OffsetDateTime.now());
        updateContractOffer(contractOffer, fetchedContractOffer);
        return null;
    }

    public boolean updateContractOffer(DataOfferContractOfferRecord contractOffer, FetchedDataOfferContractOffer fetchedContractOffer) {
        if (!Objects.equals(contractOffer.getPolicy().data(), fetchedContractOffer.getPolicyJson())) {
            contractOffer.setPolicy(JSONB.jsonb(fetchedContractOffer.getPolicyJson()));
            contractOffer.setUpdatedAt(OffsetDateTime.now());
            return true;
        }
        return false;
    }
}
