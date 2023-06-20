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

package de.sovity.edc.ext.brokerserver.services.api;

import de.sovity.edc.ext.brokerserver.dao.pages.catalog.models.ContractOfferRs;
import de.sovity.edc.ext.brokerserver.dao.pages.dataoffer.DataOfferDetailPageQueryService;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferDetailPageQuery;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferDetailPageResult;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferListEntryContractOffer;
import de.sovity.edc.ext.wrapper.api.common.model.PolicyDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class DataOfferDetailApiService {
    private final DataOfferDetailPageQueryService dataOfferDetailPageQueryService;

    public DataOfferDetailPageResult dataOfferDetailPage(DSLContext dsl, DataOfferDetailPageQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        var dataOffer = dataOfferDetailPageQueryService.queryDataOfferDetailsPage(dsl, query.getAssetId(), query.getConnectorEndpoint());

        var result = new DataOfferDetailPageResult();
        result.setAssetId(dataOffer.getAssetId());
        result.setConnectorEndpoint(dataOffer.getConnectorEndpoint());
        result.setConnectorOnlineStatus(mapConnectorOnlineStatus(dataOffer.getConnectorOnlineStatus()));
        result.setConnectorOfflineSinceOrLastUpdatedAt(dataOffer.getConnectorOfflineSinceOrLastUpdatedAt());
        result.setCreatedAt(dataOffer.getCreatedAt());
        result.setUpdatedAt(dataOffer.getUpdatedAt());
        result.setContractOffers(mapContractOffer(dataOffer.getContractOffers()));
        return result;
    }

    private de.sovity.edc.ext.wrapper.api.broker.model.ConnectorOnlineStatus mapConnectorOnlineStatus(de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus connectorOnlineStatus) {
        if (connectorOnlineStatus == null) {
            return de.sovity.edc.ext.wrapper.api.broker.model.ConnectorOnlineStatus.OFFLINE;
        }

        return switch (connectorOnlineStatus) {
            case ONLINE -> de.sovity.edc.ext.wrapper.api.broker.model.ConnectorOnlineStatus.ONLINE;
            case OFFLINE -> de.sovity.edc.ext.wrapper.api.broker.model.ConnectorOnlineStatus.OFFLINE;
        };
    }

    private List<DataOfferListEntryContractOffer> mapContractOffer(List<ContractOfferRs> contractOffer) {
        List<DataOfferListEntryContractOffer> result = new ArrayList<>();

        if (contractOffer == null) {
            return result;
        }

        for (var offer : contractOffer) {
            var newOffer = new DataOfferListEntryContractOffer();
            newOffer.setCreatedAt(offer.getCreatedAt());
            newOffer.setUpdatedAt(offer.getUpdatedAt());
            newOffer.setContractOfferId(offer.getContractOfferId());
            newOffer.setContractPolicy(new PolicyDto(offer.getPolicyJson()));
            result.add(newOffer);
        }
        return result;
    }
}
