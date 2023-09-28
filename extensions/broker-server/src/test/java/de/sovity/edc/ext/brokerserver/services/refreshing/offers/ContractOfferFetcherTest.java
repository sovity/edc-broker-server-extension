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
 *       sovity GmbH - initial implementation
 *
 */

package de.sovity.edc.ext.brokerserver.services.refreshing.offers;

import de.sovity.edc.utils.catalog.DspCatalogService;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.eclipse.edc.connector.spi.catalog.CatalogService;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.response.StatusResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractOfferFetcherTest {
    DataOfferFetcher contractOfferFetcher;
    CatalogService catalogService;
    DspCatalogService dspCatalogService;
    String connectorEndpoint = "http://localhost:11003/api/v1/dsp";

    @BeforeEach
    void setUp() {
        catalogService = mock(CatalogService.class);
        dspCatalogService = mock(DspCatalogService.class);
        contractOfferFetcher = new DataOfferFetcher(dspCatalogService);
    }

    @Test
    void testContractOfferFetcher() {
        // arrange
        var catalogJson = readFile("catalogResponse.json");
        var result = CompletableFuture.completedFuture(StatusResult.success(catalogJson.getBytes(StandardCharsets.UTF_8)));
        when(catalogService.requestCatalog(eq(connectorEndpoint), eq("dataspace-protocol-http"), eq(QuerySpec.max()))).thenReturn(result);

        // act
        var actual = contractOfferFetcher.fetch(connectorEndpoint);

        // assert
        assertThat(actual).hasSize(1);
    }

    @SneakyThrows
    private String readFile(String fileName) {
        var is = getClass().getResourceAsStream(fileName);
        Objects.requireNonNull(is, "File not found: " + fileName);
        return IOUtils.toString(is, StandardCharsets.UTF_8);
    }
}
