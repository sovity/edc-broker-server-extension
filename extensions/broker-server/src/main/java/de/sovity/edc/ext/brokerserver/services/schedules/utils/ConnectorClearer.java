package de.sovity.edc.ext.brokerserver.services.schedules.utils;

import de.sovity.edc.ext.brokerserver.dao.utils.PostgresqlUtils;
import de.sovity.edc.ext.brokerserver.db.jooq.Tables;
import org.jooq.DSLContext;

import java.util.Collection;

public class ConnectorClearer {
    public static void removeData(DSLContext dsl, Collection<String> endpoints) {
        var doco = Tables.DATA_OFFER_CONTRACT_OFFER;
        var dof = Tables.DATA_OFFER;
        dsl.deleteFrom(doco).where(PostgresqlUtils.in(doco.CONNECTOR_ENDPOINT, endpoints)).execute();
        dsl.deleteFrom(dof).where(PostgresqlUtils.in(dof.CONNECTOR_ENDPOINT, endpoints)).execute();
    }
}
