package com.weather.sdk;

import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherSDKTest {
    
    private static final String VALID_API_KEY = "test-api-key";
    private static final String TEST_CITY = "London";
    
    private WeatherSDK sdk;
    
    @BeforeEach
    void setUp() throws WeatherSDKException {
        sdk = new WeatherSDK(VALID_API_KEY, WeatherSDK.OperationMode.ON_DEMAND);
    }
    
    @AfterEach
    void tearDown() {
        if (sdk != null) {
            sdk.close();
        }
    }
    
    @Test
    void testConstructorWithNullApiKey() {
        assertThrows(WeatherSDKException.class, () -> 
            new WeatherSDK(null, WeatherSDK.OperationMode.ON_DEMAND)
        );
    }
    
    @Test
    void testConstructorWithEmptyApiKey() {
        assertThrows(WeatherSDKException.class, () -> 
            new WeatherSDK("", WeatherSDK.OperationMode.ON_DEMAND)
        );
    }
    
    @Test
    void testGetWeatherWithNullCityName() {
        assertThrows(WeatherSDKException.class, () -> 
            sdk.getWeather(null)
        );
    }
    
    @Test
    void testGetWeatherWithEmptyCityName() {
        assertThrows(WeatherSDKException.class, () -> 
            sdk.getWeather("")
        );
    }
    
    @Test
    void testGetMode() {
        assertEquals(WeatherSDK.OperationMode.ON_DEMAND, sdk.getMode());
    }
    
    @Test
    void testClearCache() throws WeatherSDKException {
        // Requires a valid API key for the actual test
        sdk.clearCache();
        assertEquals(0, sdk.getCachedCitiesCount());
    }
    
    @Test
    void testPollingModeInitialization() throws WeatherSDKException {
        try (WeatherSDK pollingSdk = new WeatherSDK(VALID_API_KEY, WeatherSDK.OperationMode.POLLING)) {
            assertEquals(WeatherSDK.OperationMode.POLLING, pollingSdk.getMode());
        }
    }
}
