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

    public Object getSetting(String name, Object defaultValue) {
        var bss = Tables.BROKER_SERVER_SETTINGS;
        var value = dslContext.select(bss.VALUE)
                .from(bss)
                .where(bss.NAME.eq(name))
                .fetchOne(bss.VALUE);

        return value.equals("") ? defaultValue : value;
    }
}
