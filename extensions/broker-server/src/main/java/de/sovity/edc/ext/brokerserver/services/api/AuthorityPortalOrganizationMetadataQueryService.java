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

import de.sovity.edc.ext.brokerserver.api.model.AuthorityPortalOrganizationMetadata;
import de.sovity.edc.ext.brokerserver.db.jooq.tables.records.OrganizationMetadataRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.util.List;

@RequiredArgsConstructor
public class AuthorityPortalOrganizationMetadataQueryService {

    public void addOrganizationMetadataEntries(DSLContext dsl, List<AuthorityPortalOrganizationMetadata> organizationMetadata) {
        var orgMetadataRecords = organizationMetadata.stream().map( it -> {
            var record = new OrganizationMetadataRecord();
            record.setMdsId(it.getMdsId());
            record.setName(it.getName());

            return record;
        }).toList();

        if (organizationMetadata.isEmpty()) {
            return;
        }

        dsl.batchStore(orgMetadataRecords).execute();
    }
}
