package com.weather.sdk;

import com.weather.sdk.config.OperationMode;
import com.weather.sdk.exception.WeatherSDKException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeatherSDK
 */
class WeatherSDKTest {
    
    private static final String VALID_API_KEY = "test-api-key";
    private static final String TEST_CITY = "London";
    
    private WeatherSDK sdk;
    
    @BeforeEach
    void setUp() throws WeatherSDKException {
        sdk = new WeatherSDK(VALID_API_KEY, OperationMode.ON_DEMAND);
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
            new WeatherSDK(null, OperationMode.ON_DEMAND)
        );
    }
    
    @Test
    void testConstructorWithEmptyApiKey() {
        assertThrows(WeatherSDKException.class, () -> 
            new WeatherSDK("", OperationMode.ON_DEMAND)
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
        assertEquals(OperationMode.ON_DEMAND, sdk.getMode());
    }
    
    @Test
    void testClearCache() {
        sdk.clearCache();
        assertEquals(0, sdk.getCachedCitiesCount());
    }
    
    @Test
    void testPollingModeInitialization() throws WeatherSDKException {
        try (WeatherSDK pollingSdk = new WeatherSDK(VALID_API_KEY, OperationMode.POLLING)) {
            assertEquals(OperationMode.POLLING, pollingSdk.getMode());
        }
    }
    
    @Test
    void testSdkIsClosed() throws WeatherSDKException {
        WeatherSDK testSdk = new WeatherSDK(VALID_API_KEY, OperationMode.ON_DEMAND);
        assertFalse(testSdk.isClosed());
        
        testSdk.close();
        assertTrue(testSdk.isClosed());
    }
    
    @Test
    void testGetWeatherAfterClose() {
        sdk.close();
        assertThrows(WeatherSDKException.class, () -> 
            sdk.getWeather(TEST_CITY)
        );
    }
}
