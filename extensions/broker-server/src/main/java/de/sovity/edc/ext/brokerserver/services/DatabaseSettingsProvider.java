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

package de.sovity.edc.ext.brokerserver.services;

import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

@RequiredArgsConstructor
public class DatabaseSettingsProvider {
    private final DSLContext dslContext;

    public String getSettingString(String name, String defaultValue) {
        var value = getDbValue(name);
        return (value == null || value.equals("")) ? defaultValue : value;
    }

    public Integer getSettingInteger(String name, Integer defaultValue) {
        var value = getDbValue(name);
        return (value == null || value.equals("")) ? defaultValue : Integer.parseInt(value);
    }

    private String getDbValue(String name) {
        var bss = Tables.BROKER_SERVER_SETTINGS;
        validateTableExists(); //needed for tests

        return dslContext.select(bss.VALUE)
            .from(bss)
            .where(bss.NAME.eq(name))
            .fetchOne(bss.VALUE);
    }

    private void validateTableExists() {
        var bss = Tables.BROKER_SERVER_SETTINGS;
        var tableExists = dslContext.meta().getTables().contains(bss);
        if (!tableExists) {
            dslContext.createTable(bss)
                    .column(bss.NAME)
                    .column(bss.VALUE)
                    .execute();
        }
    }
}
