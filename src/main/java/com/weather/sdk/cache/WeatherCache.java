package com.weather.sdk.cache;

import com.weather.sdk.model.WeatherData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * LRU cache for storing weather data
 *
 * Automatically deletes the oldest records when the limit is exceeded.
 * Maximum capacity: 10 cities.
 */
public class WeatherCache {
    
    private final int maxSize;
    private final Map<String, WeatherData> cache;

    /**
     * Creates a cache with the specified size
     *
     * @param maxSize is the maximum number of cities in the cache
     */
    public WeatherCache(int maxSize) {
        this.maxSize = maxSize;
        // LinkedHashMap with accessOrder=true for LRU
        this.cache = new LinkedHashMap<String, WeatherData>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, WeatherData> eldest) {
                return size() > WeatherCache.this.maxSize;
            }
        };
    }

    /**
     * Gets weather data from the cache
     *
     * @param cityName : city name
     * @return weather data or null if not found
     */
    public synchronized WeatherData get(String cityName) {
        return cache.get(normalizeCityName(cityName));
    }

    /**
     * Stores weather data in the cache
     *
     * @param cityName : city name
     * @param data : weather data
     */
    public synchronized void put(String cityName, WeatherData data) {
        cache.put(normalizeCityName(cityName), data);
    }

    /**
     * Removes weather data from the cache
     *
     * @param cityName city name
     */
    public synchronized void remove(String cityName) {
        cache.remove(normalizeCityName(cityName));
    }

    /**
     * Clears the entire cache
     */
    public synchronized void clear() {
        cache.clear();
    }

    /**
     * Returns the number of cities in the cache
     */
    public synchronized int size() {
        return cache.size();
    }

    /**
     * Returns a set of city names in the cache
     */
    public synchronized Set<String> getCityNames() {
        return Set.copyOf(cache.keySet());
    }

    /**
     * Normalizes the city name for use as a key
     */
    private String normalizeCityName(String cityName) {
        return cityName.trim().toLowerCase();
    }
}
