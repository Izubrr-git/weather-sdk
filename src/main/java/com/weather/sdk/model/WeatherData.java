package com.weather.sdk.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Wrapper for cached weather data with validation.
 * <p>
 * Stores WeatherResponse and timestamp to check data freshness.
 * Data is considered valid if less than 10 minutes have passed.
 */
public class WeatherData {

    private static final long CACHE_VALIDITY_MINUTES = 10;

    private final WeatherResponse weatherResponse;
    private final Instant timestamp;

    /**
     * Creates a wrapper with current timestamp
     *
     * @param weatherResponse weather data
     */
    public WeatherData(WeatherResponse weatherResponse) {
        if (weatherResponse == null) {
            throw new IllegalArgumentException("WeatherResponse cannot be null");
        }
        this.weatherResponse = weatherResponse;
        this.timestamp = Instant.now();
    }

    /**
     * Checks if data is still valid (less than 10 minutes have passed)
     *
     * @return true if data is valid, false if expired
     */
    public boolean isValid() {
        Instant now = Instant.now();
        long minutesPassed = ChronoUnit.MINUTES.between(timestamp, now);
        return minutesPassed < CACHE_VALIDITY_MINUTES;
    }

    /**
     * Returns weather data
     */
    public WeatherResponse getWeatherResponse() {
        return weatherResponse;
    }

    /**
     * Returns data creation timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns number of minutes since data was created
     */
    public long getAgeMinutes() {
        return ChronoUnit.MINUTES.between(timestamp, Instant.now());
    }
}
