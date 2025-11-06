package com.weather.sdk.cache;

import com.weather.sdk.model.WeatherData;

/**
 * A cache entry containing weather data and a timestamp.
 */
public class CacheEntry {
    
    private final WeatherData data;
    private final long timestamp;

    public CacheEntry(WeatherData data, long timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    public WeatherData getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
