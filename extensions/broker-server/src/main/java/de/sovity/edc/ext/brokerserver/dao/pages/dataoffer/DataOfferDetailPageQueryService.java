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

import de.sovity.edc.ext.brokerserver.dao.pages.catalog.CatalogQueryContractOfferFetcher;
import de.sovity.edc.ext.brokerserver.dao.pages.catalog.CatalogQueryFields;
import de.sovity.edc.ext.brokerserver.dao.pages.catalog.models.DataOfferDetailRs;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.services.config.BrokerServerSettings;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

@RequiredArgsConstructor
public class DataOfferDetailPageQueryService {
    private final CatalogQueryContractOfferFetcher catalogQueryContractOfferFetcher;
    private final BrokerServerSettings brokerServerSettings;

    public DataOfferDetailRs queryDataOfferDetailsPage(DSLContext dsl, String assetId, String endpoint) {
        var d = Tables.DATA_OFFER;
        var c = Tables.CONNECTOR;

        var fields = new CatalogQueryFields(
                Tables.CONNECTOR,
                Tables.DATA_OFFER,
                brokerServerSettings.getDataSpaceConfig()
        );

        return dsl.select(
                d.ASSET_ID,
                d.ASSET_PROPERTIES.cast(String.class).as("assetPropertiesJson"),
                d.CREATED_AT,
                d.UPDATED_AT,
                catalogQueryContractOfferFetcher.getContractOffers(fields).as("contractOffers"),
                c.ENDPOINT.as("connectorEndpoint"),
                c.ONLINE_STATUS.as("connectorOnlineStatus"))
                .from(d).leftJoin(c).on(c.ENDPOINT.eq(d.CONNECTOR_ENDPOINT))
                .where(d.ASSET_ID.eq(assetId).or(d.CONNECTOR_ENDPOINT.eq(endpoint)))
                .fetchOneInto(DataOfferDetailRs.class);
    }
}
