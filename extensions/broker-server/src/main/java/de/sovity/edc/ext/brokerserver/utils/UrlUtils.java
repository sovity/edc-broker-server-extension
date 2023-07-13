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

package de.sovity.edc.ext.brokerserver.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {


    /**
     * Returns everything before the URLs path.
     * <p>
     * Example: http://www.example.com/path/to/my/file.html -> http://www.example.com
     * Example 2: http://www.example.com:9000/path/to/my/file.html -> http://www.example.com:9000
     *
     * @param url url
     * @return protocol, host, port
     */
    public static String getEverythingBeforeThePath(String url) {
        var uri = URI.create(url);
        var scheme = uri.getScheme(); // "http"
        var authority = uri.getAuthority(); // "www.example.com"
        int port = uri.getPort(); // -1 (no port specified)
        var everythingBeforePath = scheme + "://" + authority;
        if (port != -1) {
            everythingBeforePath += ":" + port;
        }
        return everythingBeforePath;
    }

    public static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }
}
