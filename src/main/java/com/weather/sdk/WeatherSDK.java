package com.weather.sdk;

import com.weather.sdk.cache.WeatherCache;
import com.weather.sdk.client.OpenWeatherClient;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherData;
import com.weather.sdk.model.WeatherResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SDK for working with the OpenWeather API
 *
 * Supports two modes:
 * - ON_DEMAND: data updates only on demand
 * - POLLING: data updates automatically every 10 minutes
 *
 * Usage example:
 * <pre>
 * WeatherSDK sdk = new WeatherSDK("your-api-key", OperationMode.ON_DEMAND);
 * WeatherResponse weather = sdk.getWeather("London");
 * System.out.println(weather.getName() + ": " + weather.getTemperature().getTemp() + "K");
 * sdk.close();
 * </pre>d
 */
public class WeatherSDK implements AutoCloseable {
    
    private static final Logger LOGGER = Logger.getLogger(WeatherSDK.class.getName());
    private static final int POLLING_INTERVAL_MINUTES = 10;
    
    private final String apiKey;
    private final OperationMode mode;
    private final OpenWeatherClient client;
    private final WeatherCache cache;
    private ScheduledExecutorService scheduler;

    /**
     * SDK Operating Modes
     */
    public enum OperationMode {
        /** Updating data only upon user request */
        ON_DEMAND,
        /** Automatic periodic data update */
        POLLING
    }

    /**
     * Creates an SDK instance
     *
     * @param apiKey OpenWeather API key
     * @param mode SDK operating mode
     * @throws WeatherSDKException if the apiKey is empty or null
     */
    public WeatherSDK(String apiKey, OperationMode mode) throws WeatherSDKException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSDKException("API key cannot be null or empty");
        }
        
        this.apiKey = apiKey;
        this.mode = mode != null ? mode : OperationMode.ON_DEMAND;
        this.client = new OpenWeatherClient(apiKey);
        this.cache = new WeatherCache(10);

        if (this.mode == OperationMode.POLLING) {
            startPolling();
        }
        
        LOGGER.log(Level.INFO, "WeatherSDK initialized in {0} mode", this.mode);
    }

    /**
     * Gets weather information for the specified city
     *
     * @param cityName - city name
     * @return weather data
     * @throws WeatherSDKException if the request fails
     */
    public WeatherResponse getWeather(String cityName) throws WeatherSDKException {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new WeatherSDKException("City name cannot be null or empty");
        }
        
        String normalizedCity = cityName.trim();

        WeatherData cachedData = cache.get(normalizedCity);
        if (cachedData != null && cachedData.isValid()) {
            LOGGER.log(Level.FINE, "Returning cached data for {0}", normalizedCity);
            return cachedData.getWeatherResponse();
        }

        return fetchAndCacheWeather(normalizedCity);
    }

    /**
     * Clears the entire cache
     */
    public void clearCache() {
        cache.clear();
        LOGGER.log(Level.INFO, "Cache cleared");
    }
    
    /**
     * Возвращает текущий режим работы SDK
     */
    public OperationMode getMode() {
        return mode;
    }

    /**
     * Returns the current SDK operating mode
     */
    public int getCachedCitiesCount() {
        return cache.size();
    }

    /**
     * Requests data from the API and stores it in the cache
     */
    private WeatherResponse fetchAndCacheWeather(String cityName) throws WeatherSDKException {
        try {
            WeatherResponse response = client.getCurrentWeather(cityName);
            cache.put(cityName, new WeatherData(response));
            LOGGER.log(Level.FINE, "Fetched and cached weather for {0}", cityName);
            return response;
        } catch (Exception e) {
            throw new WeatherSDKException("Failed to fetch weather for " + cityName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Starts periodic cache refresh
     */
    private void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "WeatherSDK-Polling");
            thread.setDaemon(true);
            return thread;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateAllCachedCities();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during polling update", e);
            }
        }, POLLING_INTERVAL_MINUTES, POLLING_INTERVAL_MINUTES, TimeUnit.MINUTES);
        
        LOGGER.log(Level.INFO, "Polling started with interval: {0} minutes", POLLING_INTERVAL_MINUTES);
    }

    /**
     * Updates data for all cities in the cache
     */
    private void updateAllCachedCities() {
        for (String cityName : cache.getCityNames()) {
            try {
                fetchAndCacheWeather(cityName);
                LOGGER.log(Level.FINE, "Updated weather for {0}", cityName);
            } catch (WeatherSDKException e) {
                LOGGER.log(Level.WARNING, "Failed to update weather for {0}: {1}", 
                          new Object[]{cityName, e.getMessage()});
            }
        }
    }

    /**
     * Closes the SDK and frees resources
     */
    @Override
    public void close() {
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
            LOGGER.log(Level.INFO, "WeatherSDK closed");
        }
    }
}
