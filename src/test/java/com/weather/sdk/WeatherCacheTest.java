package com.weather.sdk;

import com.weather.sdk.cache.WeatherCache;
import com.weather.sdk.model.WeatherData;
import com.weather.sdk.model.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeatherCache
 * 
 * These tests don't require API access - they test cache logic only
 */
class WeatherCacheTest {
    
    private WeatherCache cache;
    
    @BeforeEach
    void setUp() {
        cache = new WeatherCache(10);
    }
    
    /**
     * Helper method to create a mock WeatherData
     */
    private WeatherData createMockWeatherData(String cityName) {
        WeatherResponse response = new WeatherResponse();
        response.setName(cityName);
        
        WeatherResponse.Temperature temp = new WeatherResponse.Temperature(290.15, 288.0);
        response.setTemperature(temp);
        
        WeatherResponse.Weather weather = new WeatherResponse.Weather("Clear", "clear sky");
        response.setWeather(weather);
        
        WeatherResponse.Wind wind = new WeatherResponse.Wind(5.5);
        response.setWind(wind);
        
        response.setVisibility(10000);
        response.setDatetime(System.currentTimeMillis() / 1000);
        
        WeatherResponse.Sys sys = new WeatherResponse.Sys(1234567890L, 1234567900L);
        response.setSys(sys);
        
        response.setTimezone(3600);
        
        return new WeatherData(response);
    }
    
    @Test
    void testPutAndGet() {
        WeatherData data = createMockWeatherData("London");
        cache.put("London", data);
        
        WeatherData retrieved = cache.get("London");
        assertNotNull(retrieved);
        assertEquals("London", retrieved.getWeatherResponse().getName());
    }
    
    @Test
    void testGetNonExistent() {
        WeatherData retrieved = cache.get("NonExistent");
        assertNull(retrieved);
    }
    
    @Test
    void testCaseInsensitivity() {
        WeatherData data = createMockWeatherData("London");
        cache.put("London", data);
        
        WeatherData retrieved = cache.get("london");
        assertNotNull(retrieved);
        assertEquals("London", retrieved.getWeatherResponse().getName());
    }
    
    @Test
    void testRemove() {
        WeatherData data = createMockWeatherData("London");
        cache.put("London", data);
        
        cache.remove("London");
        
        WeatherData retrieved = cache.get("London");
        assertNull(retrieved);
    }
    
    @Test
    void testClear() {
        cache.put("London", createMockWeatherData("London"));
        cache.put("Paris", createMockWeatherData("Paris"));
        
        cache.clear();
        
        assertEquals(0, cache.size());
    }
    
    @Test
    void testSize() {
        assertEquals(0, cache.size());
        
        cache.put("London", createMockWeatherData("London"));
        assertEquals(1, cache.size());
        
        cache.put("Paris", createMockWeatherData("Paris"));
        assertEquals(2, cache.size());
    }
    
    @Test
    void testMaxSize() {
        // Add 11 cities to cache with max size 10
        for (int i = 1; i <= 11; i++) {
            String cityName = "City" + i;
            cache.put(cityName, createMockWeatherData(cityName));
        }
        
        // Cache should contain only 10 cities
        assertEquals(10, cache.size());
        
        // First city should be evicted (LRU)
        assertNull(cache.get("City1"));
        
        // Last city should be present
        assertNotNull(cache.get("City11"));
    }
    
    @Test
    void testLRUBehavior() {
        // Fill cache to max
        for (int i = 1; i <= 10; i++) {
            cache.put("City" + i, createMockWeatherData("City" + i));
        }
        
        // Access City1 to make it recently used
        cache.get("City1");
        
        // Add one more city
        cache.put("City11", createMockWeatherData("City11"));
        
        // City1 should still be in cache (it was accessed recently)
        assertNotNull(cache.get("City1"));
        
        // City2 should be evicted (it's the oldest non-accessed)
        assertNull(cache.get("City2"));
    }
    
    @Test
    void testGetCityNames() {
        cache.put("London", createMockWeatherData("London"));
        cache.put("Paris", createMockWeatherData("Paris"));
        cache.put("Tokyo", createMockWeatherData("Tokyo"));
        
        Set<String> cityNames = cache.getCityNames();
        
        assertEquals(3, cityNames.size());
        assertTrue(cityNames.contains("london")); // normalized
        assertTrue(cityNames.contains("paris"));
        assertTrue(cityNames.contains("tokyo"));
    }
    
    @Test
    void testContains() {
        assertFalse(cache.contains("London"));
        
        cache.put("London", createMockWeatherData("London"));
        
        assertTrue(cache.contains("London"));
        assertTrue(cache.contains("london")); // case insensitive
    }
    
    @Test
    void testPutNullData() {
        assertThrows(IllegalArgumentException.class, () -> 
            cache.put("London", null)
        );
    }
    
    @Test
    void testGetNullCityName() {
        assertThrows(IllegalArgumentException.class, () -> 
            cache.get(null)
        );
    }
    
    @Test
    void testNormalizeCityNameWithSpaces() {
        WeatherData data = createMockWeatherData("New York");
        cache.put("  New York  ", data); // with spaces
        
        WeatherData retrieved = cache.get("new york"); // normalized
        assertNotNull(retrieved);
    }
}
