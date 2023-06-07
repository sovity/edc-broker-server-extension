package de.sovity.edc.ext.brokerserver.dao.pages.catalog.models;

import de.sovity.edc.ext.brokerserver.dao.pages.catalog.CatalogQueryFields;
import org.jooq.Field;

@FunctionalInterface
public interface AvailableFilterValuesQuery {

    /**
     * Gets the values for a given filter attribute from a list of data offers.
     *
     * @param fields a
     * @return field / multiset field that will contain the available values
     */
    Field<String> getAttributeValueField(CatalogQueryFields fields);
}
