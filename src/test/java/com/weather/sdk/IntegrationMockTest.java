package com.weather.sdk;

import com.weather.sdk.client.WeatherApiClient;
import com.weather.sdk.config.OperationMode;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for WeatherSDK
 *
 * These tests verify the full SDK workflow with mocked API responses.
 * No real API key needed!
 */
@ExtendWith(MockitoExtension.class)
class IntegrationMockTest {

    @Mock
    private WeatherApiClient mockApiClient;

    private WeatherSDK sdk;
    private static final String MOCK_API_KEY = "mock-test-key";

    @BeforeEach
    void setUp() throws WeatherSDKException {
        // Create SDK with mock client
        sdk = new WeatherSDK(MOCK_API_KEY, OperationMode.ON_DEMAND, mockApiClient);
    }

    @AfterEach
    void tearDown() {
        if (sdk != null && !sdk.isClosed()) {
            sdk.close();
        }
    }

    /**
     * Helper method to create mock weather response
     */
    private WeatherResponse createMockResponse(String city, double tempKelvin) {
        WeatherResponse response = new WeatherResponse();
        response.setName(city);

        WeatherResponse.Temperature temp = new WeatherResponse.Temperature(tempKelvin, tempKelvin - 2);
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

    /**
     * Test: Full workflow from SDK creation to weather retrieval
     */
    @Test
    void testFullWorkflowOnDemandMode() throws WeatherSDKException {
        // Given - SDK is initialized in ON_DEMAND mode
        assertEquals(OperationMode.ON_DEMAND, sdk.getMode());
        assertEquals(0, sdk.getCachedCitiesCount());

        // Mock API response
        WeatherResponse mockResponse = createMockResponse("London", 293.15);
        when(mockApiClient.getCurrentWeather("London")).thenReturn(mockResponse);

        // When - get weather
        WeatherResponse result = sdk.getWeather("London");

        // Then
        assertNotNull(result);
        assertEquals("London", result.getName());
        assertEquals(293.15, result.getTemperature().getTemp());
        assertEquals(1, sdk.getCachedCitiesCount());

        // Clear cache
        assertDoesNotThrow(() -> sdk.clearCache());
        assertEquals(0, sdk.getCachedCitiesCount());
    }

    @Test
    void testCacheBehavior() throws WeatherSDKException {
        // Given - empty cache
        assertEquals(0, sdk.getCachedCitiesCount());

        // Mock response
        WeatherResponse mockResponse = createMockResponse("Paris", 290.15);
        when(mockApiClient.getCurrentWeather("Paris")).thenReturn(mockResponse);

        // When - get weather and populate cache
        sdk.getWeather("Paris");
        assertEquals(1, sdk.getCachedCitiesCount());

        // When - clear cache multiple times
        sdk.clearCache();
        sdk.clearCache();

        // Then - should not throw exceptions
        assertEquals(0, sdk.getCachedCitiesCount());
    }

    @Test
    void testPollingModeInitialization() throws WeatherSDKException {
        // Given/When - create SDK in POLLING mode
        try (WeatherSDK pollingSdk = new WeatherSDK(MOCK_API_KEY, OperationMode.POLLING, mockApiClient)) {

            // Then
            assertEquals(OperationMode.POLLING, pollingSdk.getMode());
            assertFalse(pollingSdk.isClosed());
            assertEquals(0, pollingSdk.getCachedCitiesCount());
        }

        // SDK should be closed after try-with-resources
    }

    @Test
    void testMultipleCities() throws WeatherSDKException {
        // Given - SDK in ON_DEMAND mode
        String[] cities = {"London", "Paris", "Tokyo"};

        // Mock responses
        for (String city : cities) {
            WeatherResponse mockResponse = createMockResponse(city, 290.0);
            when(mockApiClient.getCurrentWeather(city)).thenReturn(mockResponse);
        }

        // When - request weather for multiple cities
        assertDoesNotThrow(() -> {
            for (String city : cities) {
                WeatherResponse response = sdk.getWeather(city);
                assertNotNull(response);
                assertEquals(city, response.getName());
            }
        });

        // Then
        assertEquals(3, sdk.getCachedCitiesCount());
    }

    @Test
    void testSdkClose() throws WeatherSDKException {
        // Given - SDK is open with cached data
        WeatherResponse mockResponse = createMockResponse("Berlin", 288.15);
        when(mockApiClient.getCurrentWeather("Berlin")).thenReturn(mockResponse);

        sdk.getWeather("Berlin");
        assertFalse(sdk.isClosed());
        assertEquals(1, sdk.getCachedCitiesCount());

        // When - close SDK
        sdk.close();

        // Then
        assertTrue(sdk.isClosed());

        // And - cache should be cleared
        assertEquals(0, sdk.getCachedCitiesCount());
    }

    @Test
    void testDoubleClose() {
        // Given - SDK is open
        assertFalse(sdk.isClosed());

        // When - close twice
        sdk.close();
        sdk.close();

        // Then - should not throw exception
        assertTrue(sdk.isClosed());
        assertEquals(0, sdk.getCachedCitiesCount());
    }

    @Test
    void testOperationsAfterClose() {
        // Given - SDK is closed
        sdk.close();

        // Then - operations should throw exception
        assertThrows(WeatherSDKException.class, () ->
                sdk.getWeather("London")
        );

        assertTrue(sdk.isClosed());
    }

    @Test
    void testCacheRetrievalAfterStoringMultipleCities() throws WeatherSDKException {
        // Given - multiple cities stored in cache
        String[] cities = {"Rome", "Madrid", "Amsterdam"};

        for (String city : cities) {
            WeatherResponse mockResponse = createMockResponse(city, 295.0);
            when(mockApiClient.getCurrentWeather(city)).thenReturn(mockResponse);
            sdk.getWeather(city);
        }

        assertEquals(3, sdk.getCachedCitiesCount());

        // When - retrieve cached data (should not call API again)
        for (String city : cities) {
            WeatherResponse response = sdk.getWeather(city);
            assertNotNull(response);
            assertEquals(city, response.getName());
        }

        // Then - API should be called only once per city
        for (String city : cities) {
            verify(mockApiClient, times(1)).getCurrentWeather(city);
        }
    }

    @Test
    void testEmptyApiKeyThrowsException() {
        // When/Then
        assertThrows(WeatherSDKException.class, () ->
                new WeatherSDK("", OperationMode.ON_DEMAND, mockApiClient)
        );

        assertThrows(WeatherSDKException.class, () ->
                new WeatherSDK("   ", OperationMode.ON_DEMAND, mockApiClient)
        );
    }

    @Test
    void testNullApiKeyThrowsException() {
        // When/Then
        assertThrows(WeatherSDKException.class, () ->
                new WeatherSDK(null, OperationMode.ON_DEMAND, mockApiClient)
        );
    }

    @Test
    void testDefaultModeWhenNullProvided() throws WeatherSDKException {
        // Given/When - create SDK with null mode
        try (WeatherSDK defaultSdk = new WeatherSDK(MOCK_API_KEY, null, mockApiClient)) {

            // Then - should default to ON_DEMAND
            assertEquals(OperationMode.ON_DEMAND, defaultSdk.getMode());
        }
    }

    @Test
    void testCompleteWorkflowWithMultipleOperations() throws WeatherSDKException {
        // Given
        String city1 = "London";
        String city2 = "Paris";

        WeatherResponse response1 = createMockResponse(city1, 293.15);
        WeatherResponse response2 = createMockResponse(city2, 290.15);

        when(mockApiClient.getCurrentWeather(city1)).thenReturn(response1);
        when(mockApiClient.getCurrentWeather(city2)).thenReturn(response2);

        // When - first requests
        WeatherResponse result1 = sdk.getWeather(city1);
        WeatherResponse result2 = sdk.getWeather(city2);

        // Then
        assertEquals(2, sdk.getCachedCitiesCount());
        assertEquals(city1, result1.getName());
        assertEquals(city2, result2.getName());

        // When - second requests (from cache)
        WeatherResponse cachedResult1 = sdk.getWeather(city1);
        WeatherResponse cachedResult2 = sdk.getWeather(city2);

        // Then - same data returned
        assertEquals(result1.getName(), cachedResult1.getName());
        assertEquals(result2.getName(), cachedResult2.getName());

        // API called only once per city
        verify(mockApiClient, times(1)).getCurrentWeather(city1);
        verify(mockApiClient, times(1)).getCurrentWeather(city2);

        // When - clear cache
        sdk.clearCache();

        // Then
        assertEquals(0, sdk.getCachedCitiesCount());

        // When - request after clear
        sdk.getWeather(city1);

        // Then - API called again
        verify(mockApiClient, times(2)).getCurrentWeather(city1);
    }
}