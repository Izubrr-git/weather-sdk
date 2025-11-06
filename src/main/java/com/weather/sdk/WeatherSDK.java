package com.weather.sdk;

import com.weather.sdk.cache.WeatherCache;
import com.weather.sdk.client.WeatherApiClient;
import com.weather.sdk.config.OperationMode;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherData;
import com.weather.sdk.model.WeatherResponse;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SDK for working with OpenWeather API.
 *
 * Supports two modes:
 * - ON_DEMAND: data is updated only on demand
 * - POLLING: data is updated automatically every 5 minutes
 *
 * Usage example:
 * <pre>
 * try (WeatherSDK sdk = new WeatherSDK("your-api-key", OperationMode.ON_DEMAND)) {
 *     WeatherResponse weather = sdk.getWeather("London");
 *     System.out.println(weather.getName() + ": " + weather.getTemperature().getTempCelsius() + "°C");
 * }
 * </pre>
 */
public class WeatherSDK implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(WeatherSDK.class.getName());

    private static final int POLLING_INTERVAL_MINUTES = 5;

    private final String apiKey;
    private final OperationMode mode;
    private final WeatherApiClient client;
    private final WeatherCache cache;
    private ScheduledExecutorService scheduler;
    private volatile boolean closed = false;

    /**
     * Creates SDK instance.
     *
     * @param apiKey OpenWeather API key
     * @param mode SDK operation mode
     * @throws WeatherSDKException if apiKey is empty or null
     */
    public WeatherSDK(String apiKey, OperationMode mode) throws WeatherSDKException {
        this(apiKey, mode, null);
    }

    /**
     * Creates SDK instance with custom client (for testing).
     *
     * @param apiKey OpenWeather API key
     * @param mode SDK operation mode
     * @param client custom WeatherApiClient (null for default)
     * @throws WeatherSDKException if apiKey is empty or null
     */
    WeatherSDK(String apiKey, OperationMode mode, WeatherApiClient client) throws WeatherSDKException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSDKException("API key cannot be null or empty");
        }

        this.apiKey = apiKey.trim();
        this.mode = mode != null ? mode : OperationMode.ON_DEMAND;
        this.client = client != null ? client : new WeatherApiClient(this.apiKey);
        this.cache = new WeatherCache(10);

        if (this.mode == OperationMode.POLLING) {
            startPolling();
        }

        LOGGER.log(Level.INFO, "WeatherSDK initialized in {0} mode", this.mode);
    }

    /**
     * Gets weather information for specified city.
     *
     * @param cityName city name
     * @return weather data
     * @throws WeatherSDKException if request fails
     */
    public WeatherResponse getWeather(String cityName) throws WeatherSDKException {
        if (closed) {
            throw new WeatherSDKException("SDK is closed");
        }

        if (cityName == null || cityName.trim().isEmpty()) {
            throw new WeatherSDKException("City name cannot be null or empty");
        }

        String normalizedCity = cityName.trim();

        WeatherData cachedData = cache.get(normalizedCity);
        if (cachedData != null) {
            LOGGER.log(Level.FINE, "Returning cached data for {0} (age: {1} min)",
                    new Object[]{normalizedCity, cachedData.getAgeMinutes()});
            return cachedData.getWeatherResponse();
        }

        // Fetch from API and cache
        return fetchAndCacheWeather(normalizedCity);
    }

    /**
     * Clears entire cache.
     */
    public void clearCache() {
        cache.clear();
        LOGGER.log(Level.INFO, "Cache cleared");
    }

    /**
     * Returns current SDK operation mode.
     */
    public OperationMode getMode() {
        return mode;
    }

    /**
     * Returns number of cities in cache.
     */
    public int getCachedCitiesCount() {
        return cache.size();
    }

    /**
     * Returns API key (for use in Factory).
     */
    String getApiKey() {
        return apiKey;
    }

    /**
     * Checks if SDK is closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Fetches data from API and saves to cache.
     *
     * @param cityName city name
     * @return weather data
     * @throws WeatherSDKException on request error
     */
    private WeatherResponse fetchAndCacheWeather(String cityName) throws WeatherSDKException {
        WeatherResponse response = client.getCurrentWeather(cityName);
        cache.put(cityName, new WeatherData(response));
        LOGGER.log(Level.FINE, "Fetched and cached weather for {0}", cityName);
        return response;
    }

    private void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "WeatherSDK-Polling");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (!closed) {
                    updateAllCachedCities();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during polling update", e);
            }
        }, POLLING_INTERVAL_MINUTES, POLLING_INTERVAL_MINUTES, TimeUnit.MINUTES);

        LOGGER.log(Level.INFO, "Polling started with interval: {0} minutes",
                POLLING_INTERVAL_MINUTES);
    }

    /**
     * Updates data for all cities in cache.
     */
    private void updateAllCachedCities() {
        Set<String> citiesToUpdate = cache.getCityNames();

        for (String cityName : citiesToUpdate) {
            try {
                // Check that city is still in cache
                if (cache.contains(cityName)) {
                    fetchAndCacheWeather(cityName);
                    LOGGER.log(Level.FINE, "Updated weather for {0}", cityName);
                }
            } catch (WeatherSDKException e) {
                LOGGER.log(Level.WARNING, "Failed to update weather for {0}: {1}",
                        new Object[]{cityName, e.getMessage()});
            }
        }
    }

    /**
     * Closes SDK and releases resources.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        cache.clear();

        LOGGER.log(Level.INFO, "WeatherSDK closed and cache cleared");
    }
}