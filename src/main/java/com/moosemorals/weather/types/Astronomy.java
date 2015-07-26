/*
 * The MIT License
 *
 * Copyright 2015 osric.
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
package com.moosemorals.weather.types;

import org.joda.time.LocalTime;

/**
 * Sunrise, Sunset, Moonrise, Moonset for a location/date pair.
 *
 * @author osric
 */
public class Astronomy {

    private final LocalTime sunrise;
    private final LocalTime sunset;
    private final LocalTime moonrise;
    private final LocalTime moonset;

    /**
     * Time of sunrise (local time).
     *
     * @return LocalTime time of sunrise
     */
    public LocalTime getSunrise() {
        return sunrise;
    }

    /**
     * Time of sunset (local time).
     *
     * @return LocalTime time of sunset
     */
    public LocalTime getSunset() {
        return sunset;
    }

    /**
     * Time of moonrise (local time). Will be null when the moon doesn't rise on
     * the given day.
     *
     * @return LocalTime Time of moonrise, or null if there is no moonrise
     */
    public LocalTime getMoonrise() {
        return moonrise;
    }

    /**
     * Time of moonset (local time). Will be null when the moon doesn't set on
     * the given day.
     *
     * @return LocalTime Time of moonrise, or null if there is no moonset
     */
    public LocalTime getMoonset() {
        return moonset;
    }

    private Astronomy(LocalTime sunrise, LocalTime sunset, LocalTime moonrise, LocalTime moonset) {
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.moonrise = moonrise;
        this.moonset = moonset;
    }

    /**
     * Build an Astronomy data class. Not really useful to end users.
     */
    public static class Builder {

        private LocalTime sunrise;
        private LocalTime sunset;
        private LocalTime moonrise;
        private LocalTime moonset;

        public Builder() {
            super();
        }

        public Builder setSunrise(LocalTime sunrise) {
            this.sunrise = sunrise;
            return this;
        }

        public Builder setSunset(LocalTime sunset) {
            this.sunset = sunset;
            return this;
        }

        public Builder setMoonrise(LocalTime moonrise) {
            this.moonrise = moonrise;
            return this;
        }

        public Builder setMoonset(LocalTime moonset) {
            this.moonset = moonset;
            return this;
        }

        public Astronomy build() {
            return new Astronomy(sunrise, sunset, moonrise, moonset);
        }
    }

}
