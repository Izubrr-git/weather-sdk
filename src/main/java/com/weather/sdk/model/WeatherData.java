package com.weather.sdk.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Wrapper for cached weather data with validation
 */
public class WeatherData {
    
    private static final long CACHE_VALIDITY_MINUTES = 10;
    
    private final WeatherResponse weatherResponse;
    private final Instant timestamp;

    /**
     * Creates a wrapper with the current time
     */
    public WeatherData(WeatherResponse weatherResponse) {
        this.weatherResponse = weatherResponse;
        this.timestamp = Instant.now();
    }

    /**
     * Checks if the data is current (if it hasn't been 10 minutes)
     */
    public boolean isValid() {
        Instant now = Instant.now();
        long minutesPassed = ChronoUnit.MINUTES.between(timestamp, now);
        return minutesPassed < CACHE_VALIDITY_MINUTES;
    }
    
    public WeatherResponse getWeatherResponse() {
        return weatherResponse;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
}
