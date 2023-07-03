package de.sovity.edc.ext.brokerserver.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlUtilsTest {
    @Test
    void test_urlUtils() {
        assertTrue(UrlUtils.isValidUrl("http://localhost:8080"));
        assertTrue(UrlUtils.isValidUrl(" http://localhost:8080"));

        assertFalse(UrlUtils.isValidUrl("test"));
        assertFalse(UrlUtils.isValidUrl(""));
        assertFalse(UrlUtils.isValidUrl(" "));
        assertFalse(UrlUtils.isValidUrl(null));
    }
}
