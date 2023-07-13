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

package de.sovity.edc.ext.brokerserver.dao.pages.log;

import de.sovity.edc.ext.brokerserver.dao.pages.log.model.EventLogEntryRs;
import de.sovity.edc.ext.brokerserver.dao.pages.log.model.EventLogPageRs;
import de.sovity.edc.ext.brokerserver.dao.utils.MultisetUtils;
import de.sovity.edc.ext.brokerserver.dao.utils.SearchUtils;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.List;

public class EventLogPageQueryService {


    public Field<List<EventLogEntryRs>> queryEventLogEntries(DSLContext dsl, String eventLogId) {
        var e = Tables.BROKER_EVENT_LOG;
        var filterBySearchQuery = SearchUtils.simpleSearch(eventLogId,
            List.of(e.CONNECTOR_ENDPOINT, e.ASSET_ID, e.USER_MESSAGE));

        var query = dsl.select(e.asterisk())
            .from(e)
            .where(filterBySearchQuery)
            .limit(50);


        return MultisetUtils.multiset(query, EventLogEntryRs.class);
    }
    public EventLogPageRs queryEventLogPage(
        DSLContext dsl, String eventLogId
    ) {

        var eventLogs = queryEventLogEntries(dsl, eventLogId);

        return dsl.select(
            eventLogs.as("eventLogs")
        ).fetchOneInto(EventLogPageRs.class);
    }

}

