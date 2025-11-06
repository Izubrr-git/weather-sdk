package com.weather.sdk;

import com.weather.sdk.model.WeatherData;
import com.weather.sdk.model.WeatherResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeatherData
 * 
 * Tests data validity logic without API access
 */
class WeatherDataTest {
    
    private WeatherResponse createMockWeatherResponse() {
        WeatherResponse response = new WeatherResponse();
        response.setName("London");
        
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
        
        return response;
    }
    
    @Test
    void testWeatherDataCreation() {
        WeatherResponse response = createMockWeatherResponse();
        WeatherData data = new WeatherData(response);
        
        assertNotNull(data);
        assertNotNull(data.getWeatherResponse());
        assertNotNull(data.getTimestamp());
    }
    
    @Test
    void testIsValidWhenFresh() {
        WeatherData data = new WeatherData(createMockWeatherResponse());
        
        // Data should be valid immediately after creation
        assertTrue(data.isValid());
    }
    
    @Test
    void testGetAgeMinutes() throws InterruptedException {
        WeatherData data = new WeatherData(createMockWeatherResponse());
        
        assertEquals(0, data.getAgeMinutes());
        
        // Wait a bit and check age (this is a simplified test)
        // In real tests, you would mock time
        Thread.sleep(100); // 100ms
        
        // Age should still be 0 minutes (100ms < 1 minute)
        assertEquals(0, data.getAgeMinutes());
    }
    
    @Test
    void testGetWeatherResponse() {
        WeatherResponse response = createMockWeatherResponse();
        WeatherData data = new WeatherData(response);
        
        WeatherResponse retrieved = data.getWeatherResponse();
        
        assertEquals("London", retrieved.getName());
        assertEquals(290.15, retrieved.getTemperature().getTemp());
    }
    
    @Test
    void testConstructorWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new WeatherData(null)
        );
    }
}
