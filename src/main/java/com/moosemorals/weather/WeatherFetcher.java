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

import com.moosemorals.weather.reports.ErrorReport;
import com.moosemorals.weather.reports.FetchResult;
import com.moosemorals.weather.reports.Report;
import com.moosemorals.weather.reports.WeatherReport;
import com.moosemorals.weather.xml.ErrorParser;
import com.moosemorals.weather.xml.WeatherParser;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch weather data from the backend, using a provided HttpClient or building
 * one if needed. </p>
 *
 * Build and set options on a Fetcher using {@link Fetcher.Builder}, and then
 * call @{#fetch(String) fetch} to get the result.
 *
 * @author Osric Wilkinson <osric@fluffypeople.com>
 */
public class WeatherFetcher {

    private static final Logger log = LoggerFactory.getLogger(WeatherFetcher.class);
    private static final String ENDPOINT = "https://api.worldweatheronline.com/free/v2/weather.ashx";

    /**
     * Required link back to the API. Code that uses the api must display a link
     * to the provider. This is how they recommend you format it.
     */
    public static final String BOILERPLATE = "Powered by <a href=\"http://www.worldweatheronline.com/\" title=\"Free Weather API\" target=\"_blank\">World Weather Online</a>";

    private final String apiKey;
    private final String location;
    private final String language;
    private final int num_of_days;
    private final DateTime date;
    private final boolean forecast;
    private final boolean current;
    private final int timePeriod;

    private WeatherFetcher(String apiKey, String location, String language, int num_of_days, DateTime date, boolean forecast, boolean current, int timePeriod) {
        this.apiKey = apiKey;
        this.location = location;
        this.language = language;

        this.num_of_days = num_of_days;
        this.date = date;
        this.forecast = forecast;
        this.current = current;
        this.timePeriod = timePeriod;
    }

    /**
     * Fetch weather report using parameters set in the builder. </p>
     *
     * Returns a {@link FetchResult} that either contains a
     * {@link WeatherReport} or an {@link ErrorReport}.
     *
     * @return FetchResult containing weather data, or an error
     * @throws IOException if there are network problems
     */
    public FetchResult fetch() throws IOException {
        // Check for required parameters
        if (apiKey == null) {
            throw new NullPointerException("API key not set");
        }

        if (location == null) {
            throw new NullPointerException("Location not set");
        }

        Map<String, String> param = new HashMap<>();

        param.put("q", location);
        param.put("extra", "utcDateTime");
        param.put("num_of_days", Integer.toString(num_of_days));
        param.put("tp", Integer.toString(timePeriod));
        param.put("format", "xml");
        param.put("showlocaltime", "yes");
        param.put("includelocation", "yes");

        if (date != null) {
            param.put("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date));
        }

        if (language != null) {
            param.put("lang", language);
        }

        if (!forecast) {
            param.put("fx", "no");
        }

        if (!current) {
            param.put("cc", "no");
        }

        // For logging, build the request with a hidden api key
        param.put("key", "HIDDEN");
        String loggableTarget = Util.assembleURL(WeatherFetcher.ENDPOINT, Util.flattenMap(param));

        // For live use, build the request with the real api.
        param.put("key", apiKey);
        URL target = new URL(Util.assembleURL(WeatherFetcher.ENDPOINT, Util.flattenMap(param)));

        FetchResult.Builder resultBuilder = new FetchResult.Builder();

        log.debug("Fetching URL {}", loggableTarget);
        HttpURLConnection conn = (HttpURLConnection) target.openConnection();

        try {
            conn.connect();
            int status = conn.getResponseCode();
            log.debug("Response {}", status);

            resultBuilder.setRequestsPerSecond(Util.getIntFromHeader(conn, "x-apiaxleproxy-qps-left"));
            resultBuilder.setRequestsPerDay(Util.getIntFromHeader(conn, "x-apiaxleproxy-qpd-left"));

            if (status == 200) {

                Report report = new WeatherParser().parse(conn.getInputStream());
                if (report instanceof WeatherReport) {
                    resultBuilder.setWeather((WeatherReport) report);
                } else {
                    resultBuilder.setError((ErrorReport) report);
                }

            } else {                
                ErrorReport error  = new ErrorReport("Donwload Failure", conn.getResponseMessage());
                resultBuilder.setError(error);
            }

        } catch (XMLStreamException ex) {
            resultBuilder.setError(new ErrorReport(ex));
        }

        return resultBuilder.build();
    }

    /**
     * Build a Fetcher and set its options.
     */
    public static class Builder {

        private String apiKey;
        private String location;
        private String language;
        private int num_of_days = 3;
        private DateTime date = null;
        private boolean forecast = true;
        private boolean current = true;
        private int timePeriod = 3;

        public Builder() {
            super();
        }

        /**
         * Api key from worldweatheronline.com for their V2 API. Required, no
         * default. </p>
         *
         * You can register for a key at
         * <a href="https://developer.worldweatheronline.com/auth/register">https://developer.worldweatheronline.com/auth/register</a>
         *
         * @param apiKey String api key to use
         * @return this Builder for chaining
         * @throws IllegalArgumentException for a null apiKey
         */
        public Builder setApiKey(String apiKey) {
            if (apiKey == null) {
                throw new IllegalArgumentException("API key must not be null");
            }
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Location to fetch for. Required, no default.</p>
         *
         * May be a UK or Canadian postcode, US zip code, name of a city, IPv4
         * address (in dotted quad notation) or Latitude, Longitude pair
         * (decimal degrees, comma separated)
         *
         * @param location String name of the location. The API will work out
         * the type from context.
         * @return this Builder for chaining
         */
        public Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        /**
         * Language for human readable text. Optional, default en. </p>
         *
         * <a href="http://www.worldweatheronline.com/api/docs/multilingual.aspx">List
         * of availible languages</a>.
         *
         * @param language String ISO language code
         * @return this Builder for chaining.
         */
        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        /**
         * Number of days to fetch. Optional, default 3. </p>
         *
         * Apparently can be as high as 15, but I don't think the free api
         * supports that many.
         *
         * @param num_of_days int number of days to fetch
         * @return this Builder for chaining
         */
        public Builder setNumOfDays(int num_of_days) {
            if (num_of_days < 0) {
                throw new IllegalArgumentException("Number of days must be positive");
            }
            this.num_of_days = num_of_days;
            return this;
        }

        /**
         * Date to fetch weather for. Optional, default today. </p>
         *
         * @param date DateTime date to fetch weather for.
         * @return this Builder for chaining
         */
        public Builder setDate(DateTime date) {
            this.date = date;
            return this;
        }

        /**
         * Fetch weather forecast. Optional, default true. </p>
         *
         * False means don't fetch any forecast information.
         *
         * @param forecast boolean true to fetch forecast, false otherwise.
         * @return this Builder for chaining
         */
        public Builder setForecast(boolean forecast) {
            this.forecast = forecast;
            return this;
        }

        /**
         * Fetch current conditions. Optional, default true. </p>
         *
         * False means don't fetch current weather conditions for your location.
         *
         * @param current boolean true to fetch current conditions, false
         * otherwise.
         * @return this Builder for chaining
         */
        public Builder setCurrent(boolean current) {
            this.current = current;
            return this;
        }

        /**
         * Frequency of forecasts, in hours. Optional, default 3. </p>
         *
         * Can be 3, 6, 12 or 24. Other values are ignored by the API (and
         * revert to 3)
         *
         * @param timePeriod int hours between forecasts
         * @return this Builder for chaining
         */
        public Builder setFrequency(int timePeriod) {
            this.timePeriod = timePeriod;
            return this;
        }

        public WeatherFetcher build() {
            return new WeatherFetcher(apiKey, location, language, num_of_days, date, forecast, current, timePeriod);
        }
    }

}
