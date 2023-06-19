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

import de.sovity.edc.ext.brokerserver.dao.pages.dataoffer.DataOfferDetailPageQueryService;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferDetailPageQuery;
import de.sovity.edc.ext.wrapper.api.broker.model.DataOfferDetailPageResult;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.util.Objects;

@RequiredArgsConstructor
public class DataOfferDetailApiService {
    private final DataOfferDetailPageQueryService dataOfferDetailPageQueryService;
    private final PaginationMetadataUtils paginationMetadataUtils;

    public DataOfferDetailPageResult dataOfferDetailPage(DSLContext dsl, DataOfferDetailPageQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        var connectorDbRows = dataOfferDetailPageQueryService.queryDataOfferDetailsPage(dsl, query.getAssetId());

        var result = new DataOfferDetailPageResult();
        return result;
    }
}
