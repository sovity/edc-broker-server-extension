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
import de.sovity.edc.ext.brokerserver.dao.pages.catalog.models.DataOfferRs;
import de.sovity.edc.ext.brokerserver.dao.pages.dataoffer.DataOfferDetailPageQueryService;
import de.sovity.edc.ext.wrapper.api.broker.model.ConnectorOnlineStatus;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferDetailPageQuery;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferDetailPageResult;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferListEntry;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferListEntryContractOffer;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class DataOfferDetailApiService {
    private final DataOfferDetailPageQueryService dataOfferDetailPageQueryService;
    private final PaginationMetadataUtils paginationMetadataUtils;

    public DataOfferDetailPageResult dataOfferDetailPage(DSLContext dsl, DataOfferDetailPageQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        var dataOfferDbRow = dataOfferDetailPageQueryService.queryDataOfferDetailsPage(dsl, query.getAssetId());
        var dataOffer = buildDataOfferListEntry(dataOfferDbRow);

        var result = new DataOfferDetailPageResult();
        result.setAssetId(dataOffer.getAssetId());
        result.setConnectorEndpoint(dataOffer.getConnectorEndpoint());
        result.setConnectorOnlineStatus(dataOffer.getConnectorOnlineStatus());
        result.setConnectorOfflineSinceOrLastUpdatedAt(dataOffer.getConnectorOfflineSinceOrLastUpdatedAt());
        result.setCreatedAt(dataOffer.getCreatedAt());
        result.setUpdatedAt(dataOffer.getUpdatedAt());
        result.setContractOffers(dataOffer.getContractOffers());
        return result;
    }

    private DataOfferListEntry buildDataOfferListEntry(DataOfferRs dataOffer) {
        var dto = new DataOfferListEntry();
        dto.setAssetId(dataOffer.getAssetId());
        dto.setContractOffers(getContractOffers(dataOffer));
        dto.setConnectorEndpoint(dataOffer.getConnectorEndpoint());
        dto.setConnectorOnlineStatus(ConnectorOnlineStatus.valueOf(dataOffer.getConnectorOnlineStatus().name()));
        dto.setConnectorOfflineSinceOrLastUpdatedAt(dataOffer.getConnectorOfflineSinceOrLastUpdatedAt());
        dto.setCreatedAt(dataOffer.getCreatedAt());
        dto.setUpdatedAt(dataOffer.getUpdatedAt());
        return dto;
    }

    private List<DataOfferListEntryContractOffer> getContractOffers(DataOfferRs dataOffer) {
        List<DataOfferListEntryContractOffer> dataOfferList = new ArrayList<>();
        for (ContractOfferRs contractOffer : dataOffer.getContractOffers()) {
            var dto = new DataOfferListEntryContractOffer();
            dto.setContractOfferId(contractOffer.getContractOfferId());
            dto.setCreatedAt(contractOffer.getCreatedAt());
            dto.setUpdatedAt(contractOffer.getUpdatedAt());
            dataOfferList.add(dto);
        }

        return dataOfferList;
    }
}
