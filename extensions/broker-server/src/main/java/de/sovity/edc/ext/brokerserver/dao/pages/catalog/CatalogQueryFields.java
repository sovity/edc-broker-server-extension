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

package de.sovity.edc.ext.brokerserver.dao.pages.catalog;

import com.github.t9t.jooq.json.JsonbDSL;
import de.sovity.edc.ext.brokerserver.dao.AssetProperty;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.Connector;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.DataOffer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * Tables and fields used in the catalog page query.
 * <p>
 * Having this as a class makes access to computed fields (e.g. asset properties) easier.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CatalogQueryFields {
    Connector connectorTable;
    DataOffer dataOfferTable;

    // Asset Properties from JSON to be used in sorting / filtering
    Field<String> assetId;
    Field<String> assetName;
    Field<String> assetDescription;
    Field<String> assetKeywords;
    Field<String> dataSpace;

    // This date should always be non-null
    // It's used in the UI to display the last relevant change date of a connector
    Field<OffsetDateTime> offlineSinceOrLastUpdatedAt;

    public CatalogQueryFields(Connector connectorTable, DataOffer dataOfferTable, Map<String, String> dataSpaceMap) {
        this.connectorTable = connectorTable;
        this.dataOfferTable = dataOfferTable;
        assetId = dataOfferTable.ASSET_ID;
        assetName = dataOfferTable.ASSET_NAME;
        assetDescription = getAssetProperty(AssetProperty.DESCRIPTION);
        assetKeywords = getAssetProperty(AssetProperty.KEYWORDS);
        offlineSinceOrLastUpdatedAt = DSL.coalesce(
                connectorTable.LAST_SUCCESSFUL_REFRESH_AT,
                connectorTable.CREATED_AT
        );

        Field<String> tempDataSpace = null;
        var dataSpaceCount = dataSpaceMap.size();
        var dataSpaceConnectorEndpoints = new ArrayList<>(dataSpaceMap.keySet());
        var dataSpaceNames = new ArrayList<>(dataSpaceMap.values());
        var endpoint = connectorTable.ENDPOINT;
        for (int i = 0; i < dataSpaceCount; i++) {
            tempDataSpace = DSL.when(endpoint.eq(dataSpaceConnectorEndpoints.get(i)), dataSpaceNames.get(i)).else_(tempDataSpace);
        }

        dataSpace = tempDataSpace;
    }

    public Field<String> getAssetProperty(String name) {
        return JsonbDSL.fieldByKeyText(dataOfferTable.ASSET_PROPERTIES, name);
    }
}
