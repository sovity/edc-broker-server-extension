package de.sovity.edc.ext.brokerserver.services.api.filtering;

import de.sovity.edc.ext.brokerserver.dao.pages.catalog.models.AvailableFilterValuesQuery;

/**
 * Implementation of a filter attribute definition for the catalog.
 *
 * @param name          technical id of the attribute
 * @param label         UI showing label for the attribute
 * @param valueGetter   query existing values from DB
 * @param filterApplier apply a filter to a data offer query
 */
public record CatalogFilterAttributeDefinition(
        String name,
        String label,
        AvailableFilterValuesQuery valueGetter,
        AttributeFilterQuery filterApplier
) {
}
