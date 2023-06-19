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

package de.sovity.edc.ext.brokerserver.dao.pages.dataoffer;

import de.sovity.edc.ext.brokerserver.dao.utils.SearchUtils;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.DataOffer;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.impl.DSL;

import java.util.List;

public class DataOfferDetailPageQueryService {
    public DataOffer queryDataOfferDetailsPage(DSLContext dsl, String assetId) {
        var d = Tables.DATA_OFFER;
        var filterBySearchQuery = SearchUtils.simpleSearch(assetId, List.of(d.ASSET_NAME, d.ASSET_ID, d.CONNECTOR_ENDPOINT));
        var dataOffers = dsl.select(d.asterisk(), dataOfferCount(d.ASSET_ID).as("numDataOffers"))
                .from(d)
                .where(filterBySearchQuery)
                .orderBy(sortDataOfferDetailsPage(d))
                .fetchInto(DataOffer.class);

        if (!dataOffers.isEmpty()) {
            return dataOffers.get(0);
        } else {
            throw new IllegalArgumentException("Data offer not found");
        }
    }

    @NotNull
    private List<OrderField<?>> sortDataOfferDetailsPage(DataOffer d) {
        var alphabetically = d.ASSET_NAME.asc();
        var recentFirst = d.CREATED_AT.desc();

        return List.of(alphabetically, recentFirst);
    }

    private Field<Long> dataOfferCount(Field<String> endpoint) {
        var d = Tables.DATA_OFFER;
        return DSL.select(DSL.count()).from(d).where(d.CONNECTOR_ENDPOINT.eq(endpoint)).asField();
    }
}
