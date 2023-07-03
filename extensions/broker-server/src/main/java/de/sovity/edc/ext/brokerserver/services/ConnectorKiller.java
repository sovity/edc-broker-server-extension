package de.sovity.edc.ext.brokerserver.services;

import de.sovity.edc.ext.brokerserver.dao.utils.PostgresqlUtils;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import de.sovity.edc.ext.brokerserver.db.jooq.enums.ConnectorOnlineStatus;
import org.jooq.DSLContext;

import java.util.Collection;

public class ConnectorKiller {
    public void killConnectors(DSLContext dsl, Collection<String> endpoints) {
        var c = Tables.CONNECTOR;
        dsl.update(c).set(c.ONLINE_STATUS, ConnectorOnlineStatus.DEAD).where(PostgresqlUtils.in(c.ENDPOINT, endpoints)).execute();
    }
}
