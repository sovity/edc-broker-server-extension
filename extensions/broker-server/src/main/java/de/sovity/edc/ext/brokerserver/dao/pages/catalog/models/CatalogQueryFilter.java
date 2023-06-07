package de.sovity.edc.ext.brokerserver.dao.pages.catalog.models;

import java.util.List;

public record CatalogQueryFilter(
        String searchQuery,
        List<CatalogQuerySelectedFilterQuery> selectedFilters
) {

}
