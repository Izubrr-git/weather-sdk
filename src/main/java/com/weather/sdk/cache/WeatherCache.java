package com.weather.sdk.cache;

import com.weather.sdk.model.WeatherData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * LRU cache for storing weather data.
 * <p>
 * Automatically removes oldest entries when limit is exceeded.
 * Maximum capacity: 10 cities.
 */
public class WeatherCache {

    private final int maxSize;
    private final Map<String, WeatherData> cache;

    /**
     * Creates cache with specified size
     *
     * @param maxSize maximum number of cities in cache
     */
    public WeatherCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }
        this.maxSize = maxSize;
        // LinkedHashMap with accessOrder=true for LRU implementation
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, WeatherData> eldest) {
                return size() > WeatherCache.this.maxSize;
            }
        };
    }

    /**
     * Gets weather data from cache.
     *
     * @param cityName city name
     * @return weather data or null if not found or expired
     */
    public synchronized WeatherData get(String cityName) {
        String key = normalizeCityName(cityName);
        WeatherData data = cache.get(key);

        // Check data validity
        if (data != null && !data.isValid()) {
            cache.remove(key);
            return null;
        }

        return data;
    }

    /**
     * Stores weather data in cache
     *
     * @param cityName city name
     * @param data weather data
     */
    public synchronized void put(String cityName, WeatherData data) {
        if (data == null) {
            throw new IllegalArgumentException("WeatherData cannot be null");
        }
        cache.put(normalizeCityName(cityName), data);
    }

    /**
     * Removes weather data from cache
     *
     * @param cityName city name
     */
    public synchronized void remove(String cityName) {
        cache.remove(normalizeCityName(cityName));
    }

    /**
     * Clears entire cache
     */
    public synchronized void clear() {
        cache.clear();
    }

    /**
     * Returns number of cities in cache
     */
    public synchronized int size() {
        return cache.size();
    }

    /**
     * Returns set of city names in cache.
     *
     * @return copy of city names set
     */
    public synchronized Set<String> getCityNames() {
        return new HashSet<>(cache.keySet());
    }

    /**
     * Checks if cache contains data for specified city
     *
     * @param cityName city name
     * @return true if data exists in cache (and is valid)
     */
    public synchronized boolean contains(String cityName) {
        return get(cityName) != null;
    }

    /**
     * Normalizes city name for use as key
     *
     * @param cityName original city name
     * @return normalized name (lowercase, trimmed)
     */
    private String normalizeCityName(String cityName) {
        if (cityName == null) {
            throw new IllegalArgumentException("City name cannot be null");
        }
        return cityName.trim().toLowerCase();
    }
}
