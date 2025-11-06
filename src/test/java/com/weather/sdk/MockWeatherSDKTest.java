package com.weather.sdk;

import com.weather.sdk.client.WeatherApiClient;
import com.weather.sdk.config.OperationMode;
import com.weather.sdk.exception.ApiKeyException;
import com.weather.sdk.exception.CityNotFoundException;
import com.weather.sdk.exception.NetworkException;
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
 * Mock tests for WeatherSDK using Mockito
 *
 * These tests don't require real API access and use mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class MockWeatherSDKTest {

    @Mock
    private WeatherApiClient mockApiClient;

    private WeatherSDK sdk;
    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_CITY = "London";

    @BeforeEach
    void setUp() throws WeatherSDKException {
        // Create SDK with injected mock client
        sdk = new WeatherSDK(TEST_API_KEY, OperationMode.ON_DEMAND, mockApiClient);
    }

    @AfterEach
    void tearDown() {
        if (sdk != null && !sdk.isClosed()) {
            sdk.close();
        }
    }

    /**
     * Helper method to create a mock WeatherResponse
     */
    private WeatherResponse createMockWeatherResponse(String cityName, double temp) {
        WeatherResponse response = new WeatherResponse();
        response.setName(cityName);

        WeatherResponse.Temperature temperature = new WeatherResponse.Temperature(temp, temp - 2);
        response.setTemperature(temperature);

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
    void testGetWeatherReturnsApiResponse() throws WeatherSDKException {
        // Given
        WeatherResponse mockResponse = createMockWeatherResponse(TEST_CITY, 290.15);
        when(mockApiClient.getCurrentWeather(TEST_CITY)).thenReturn(mockResponse);

        // When
        WeatherResponse result = sdk.getWeather(TEST_CITY);

        // Then
        assertNotNull(result);
        assertEquals(TEST_CITY, result.getName());
        assertEquals(290.15, result.getTemperature().getTemp());
        assertEquals("Clear", result.getWeather().getMain());
        assertEquals("clear sky", result.getWeather().getDescription());
        verify(mockApiClient, times(1)).getCurrentWeather(TEST_CITY);
    }

    @Test
    void testGetWeatherUsesCacheOnSecondCall() throws WeatherSDKException {
        // Given
        WeatherResponse mockResponse = createMockWeatherResponse(TEST_CITY, 290.15);
        when(mockApiClient.getCurrentWeather(TEST_CITY)).thenReturn(mockResponse);

        // When - first call
        WeatherResponse result1 = sdk.getWeather(TEST_CITY);

        // When - second call (should use cache)
        WeatherResponse result2 = sdk.getWeather(TEST_CITY);

        // Then - API should be called only once
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getName(), result2.getName());
        verify(mockApiClient, times(1)).getCurrentWeather(TEST_CITY);
        assertEquals(1, sdk.getCachedCitiesCount());
    }

    @Test
    void testGetWeatherThrowsApiKeyException() throws WeatherSDKException {
        // Given
        when(mockApiClient.getCurrentWeather(TEST_CITY))
                .thenThrow(new ApiKeyException("Invalid API key"));

        // When/Then
        ApiKeyException exception = assertThrows(ApiKeyException.class,
                () -> sdk.getWeather(TEST_CITY));

        assertTrue(exception.getMessage().contains("Invalid API key"));
        verify(mockApiClient, times(1)).getCurrentWeather(TEST_CITY);
    }

    @Test
    void testGetWeatherThrowsCityNotFoundException() throws WeatherSDKException {
        // Given
        String invalidCity = "InvalidCity123";
        when(mockApiClient.getCurrentWeather(invalidCity))
                .thenThrow(new CityNotFoundException("City not found"));

        // When/Then
        CityNotFoundException exception = assertThrows(CityNotFoundException.class,
                () -> sdk.getWeather(invalidCity));

        assertTrue(exception.getMessage().contains("City not found"));
        verify(mockApiClient, times(1)).getCurrentWeather(invalidCity);
    }

    @Test
    void testGetWeatherThrowsNetworkException() throws WeatherSDKException {
        // Given
        when(mockApiClient.getCurrentWeather(TEST_CITY))
                .thenThrow(new NetworkException("Network error"));

        // When/Then
        NetworkException exception = assertThrows(NetworkException.class,
                () -> sdk.getWeather(TEST_CITY));

        assertTrue(exception.getMessage().contains("Network error"));
        verify(mockApiClient, times(1)).getCurrentWeather(TEST_CITY);
    }

    @Test
    void testCacheLimit() throws WeatherSDKException {
        // Given - mock responses for 11 cities
        String[] cities = {"City1", "City2", "City3", "City4", "City5",
                "City6", "City7", "City8", "City9", "City10", "City11"};

        for (String city : cities) {
            WeatherResponse mockResponse = createMockWeatherResponse(city, 290.0);
            when(mockApiClient.getCurrentWeather(city)).thenReturn(mockResponse);
        }

        // When - request weather for 11 cities
        for (String city : cities) {
            sdk.getWeather(city);
        }

        // Then - cache should contain only 10 cities (LRU evicted City1)
        assertEquals(10, sdk.getCachedCitiesCount());

        // Verify all cities were requested
        for (String city : cities) {
            verify(mockApiClient, times(1)).getCurrentWeather(city);
        }
    }

    @Test
    void testClearCache() throws WeatherSDKException {
        // Given
        WeatherResponse mockResponse = createMockWeatherResponse(TEST_CITY, 290.15);
        when(mockApiClient.getCurrentWeather(TEST_CITY)).thenReturn(mockResponse);

        // When
        sdk.getWeather(TEST_CITY);
        assertEquals(1, sdk.getCachedCitiesCount());

        sdk.clearCache();

        // Then
        assertEquals(0, sdk.getCachedCitiesCount());

        // When requesting again after clear
        sdk.getWeather(TEST_CITY);

        // Then - should call API again
        verify(mockApiClient, times(2)).getCurrentWeather(TEST_CITY);
    }

    @Test
    void testGetWeatherWithNullCityName() {
        // When/Then
        WeatherSDKException exception = assertThrows(WeatherSDKException.class,
                () -> sdk.getWeather(null));

        assertTrue(exception.getMessage().contains("City name cannot be null or empty"));
        verifyNoInteractions(mockApiClient);
    }

    @Test
    void testGetWeatherWithEmptyCityName() {
        // When/Then
        WeatherSDKException exception = assertThrows(WeatherSDKException.class,
                () -> sdk.getWeather("   "));

        assertTrue(exception.getMessage().contains("City name cannot be null or empty"));
        verifyNoInteractions(mockApiClient);
    }

    @Test
    void testMultipleCitiesInCache() throws WeatherSDKException {
        // Given
        String[] cities = {"London", "Paris", "Tokyo"};

        for (String city : cities) {
            WeatherResponse mockResponse = createMockWeatherResponse(city, 290.0 + cities.length);
            when(mockApiClient.getCurrentWeather(city)).thenReturn(mockResponse);
        }

        // When
        for (String city : cities) {
            WeatherResponse response = sdk.getWeather(city);
            assertNotNull(response);
            assertEquals(city, response.getName());
        }

        // Then
        assertEquals(3, sdk.getCachedCitiesCount());

        // Verify each city called once
        for (String city : cities) {
            verify(mockApiClient, times(1)).getCurrentWeather(city);
        }
    }

    @Test
    void testGetWeatherAfterClose() {
        // Given
        sdk.close();

        // When/Then
        WeatherSDKException exception = assertThrows(WeatherSDKException.class,
                () -> sdk.getWeather(TEST_CITY));

        assertTrue(exception.getMessage().contains("SDK is closed"));
        verifyNoInteractions(mockApiClient);
    }

    @Test
    void testCityNameNormalization() throws WeatherSDKException {
        // Given - different capitalizations of the same city
        String[] cityVariants = {"London", "london", "LONDON", "  London  "};
        WeatherResponse mockResponse = createMockWeatherResponse("London", 290.15);
        when(mockApiClient.getCurrentWeather(anyString())).thenReturn(mockResponse);

        // When - request each variant
        for (String variant : cityVariants) {
            sdk.getWeather(variant);
        }

        // Then - should use cache after first call (normalized to same key)
        verify(mockApiClient, times(1)).getCurrentWeather(anyString());
        assertEquals(1, sdk.getCachedCitiesCount());
    }

    @Test
    void testOperationModeOnDemand() {
        // Then
        assertEquals(OperationMode.ON_DEMAND, sdk.getMode());
        assertFalse(sdk.isClosed());
    }

    @Test
    void testCloseMultipleTimes() {
        // When
        sdk.close();
        sdk.close();
        sdk.close();

        // Then - should not throw exception
        assertTrue(sdk.isClosed());
        assertEquals(0, sdk.getCachedCitiesCount());
    }

    @Test
    void testTemperatureConversion() throws WeatherSDKException {
        // Given
        double tempKelvin = 293.15; // 20°C
        WeatherResponse mockResponse = createMockWeatherResponse(TEST_CITY, tempKelvin);
        when(mockApiClient.getCurrentWeather(TEST_CITY)).thenReturn(mockResponse);

        // When
        WeatherResponse result = sdk.getWeather(TEST_CITY);

        // Then
        assertEquals(tempKelvin, result.getTemperature().getTemp());
        assertEquals(20.0, result.getTemperature().getTempCelsius(), 0.01);
    }

    @Test
    void testWeatherResponseFields() throws WeatherSDKException {
        // Given
        WeatherResponse mockResponse = createMockWeatherResponse(TEST_CITY, 290.15);
        when(mockApiClient.getCurrentWeather(TEST_CITY)).thenReturn(mockResponse);

        // When
        WeatherResponse result = sdk.getWeather(TEST_CITY);

        // Then - verify all fields are populated
        assertNotNull(result.getName());
        assertNotNull(result.getWeather());
        assertNotNull(result.getTemperature());
        assertNotNull(result.getWind());
        assertNotNull(result.getSys());
        assertTrue(result.getVisibility() > 0);
        assertTrue(result.getDatetime() > 0);
    }
}