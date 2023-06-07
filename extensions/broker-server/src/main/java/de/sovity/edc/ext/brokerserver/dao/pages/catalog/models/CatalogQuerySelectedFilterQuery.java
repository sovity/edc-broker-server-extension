package de.sovity.edc.ext.brokerserver.dao.pages.catalog.models;

import de.sovity.edc.ext.brokerserver.dao.pages.catalog.CatalogQueryFields;
import org.jooq.Condition;

@FunctionalInterface
public interface CatalogQuerySelectedFilterQuery {

    /**
     * Adds a filter to a Catalog Query.
     *
     * @param fields fields and tables available in the catalog query
     * @return {@link Condition}
     */
    Condition filterDataOffers(CatalogQueryFields fields);
}
