/*
 * The MIT License
 *
 * Copyright 2015 Osric Wilkinson <osric@fluffypeople.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.moosemorals.weather;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A couple of utility methods that don't really belong to Fetcher.
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    /**
     * Add a query string to a URL.
     *
     * @param base String Base url. Should be well formed, but that isn't
     * checked (and doesn't make any difference to the method)
     * @param parameters String... list of key/value pairs.
     * @return String URL ready to pass to httpClient to fetch
     * @throws IllegalArgumentException if the parameters don't come in pairs
     * @throws UnsupportedEncodingException if Java doesn't know UTF-8
     */
    public static String assembleURL(String base, String... parameters) throws UnsupportedEncodingException {
        if (parameters == null || parameters.length == 0) {
            return base;
        } else if (parameters.length % 2 != 0) {
            throw new IllegalArgumentException("Parameters must come in (name, value) pairs");
        }
        StringBuilder result = new StringBuilder();
        result.append(base).append("?");
        for (int i = 0; i < parameters.length; i += 2) {
            if (i >= 2) {
                result.append("&");
            }
            result.append(URLEncoder.encode(parameters[i], "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(parameters[i + 1], "UTF-8"));
        }
        return result.toString();
    }

    /**
     * Turn a Map&lt;String, String&gt; to an array of Strings.
     *
     * @param map Map&lt;String, String&gt; to convert
     * @return String[] containg ordered key/value pairs
     */
    public static String[] flattenMap(Map<String, String> map) {
        String[] result = new String[map.size() * 2];
        int i = 0;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            result[i] = entry.getKey();
            result[i + 1] = entry.getValue();
            i += 2;
        }
        return result;
    }

    public static int getIntFromHeader(HttpURLConnection connection, String headerName) {
        return connection.getHeaderFieldInt(headerName, -1);
    }

    public static InputStream dumpInputStream(InputStream in) throws IOException {
        if (!log.isDebugEnabled()) {
            return in;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder data = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            data.append(line).append(System.lineSeparator());
        }
        log.debug("Recieved File\n------\n{}\n------", data.toString());
        return new ByteArrayInputStream(data.toString().getBytes("UTF-8"));
    }

}
