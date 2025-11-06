package com.weather.sdk;

import com.weather.sdk.config.OperationMode;
import com.weather.sdk.exception.WeatherSDKException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating and managing WeatherSDK instances.
 *
 * Ensures that only one SDK instance exists for each unique API key.
 * This prevents duplicate polling threads and optimizes resource usage.
 *
 * Usage example:
 * <pre>
 * WeatherSDK sdk1 = WeatherSDKFactory.getInstance("key1", OperationMode.POLLING);
 * WeatherSDK sdk2 = WeatherSDKFactory.getInstance("key1", OperationMode.POLLING); // returns sdk1
 *
 * WeatherSDKFactory.removeInstance("key1"); // removes and closes SDK
 * </pre>
 */
public class WeatherSDKFactory {

    private static final Logger LOGGER = Logger.getLogger(WeatherSDKFactory.class.getName());

    private static final Map<String, WeatherSDK> instances = new ConcurrentHashMap<>();

    private WeatherSDKFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Gets or creates WeatherSDK instance for specified API key.
     *
     * @param apiKey OpenWeather API key
     * @param mode SDK operation mode
     * @return WeatherSDK instance
     * @throws WeatherSDKException if SDK creation failed or mode doesn't match
     */
    public static synchronized WeatherSDK getInstance(String apiKey, OperationMode mode)
            throws WeatherSDKException {

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSDKException("API key cannot be null or empty");
        }

        if (mode == null) {
            throw new WeatherSDKException("Operation mode cannot be null");
        }

        String key = apiKey.trim();

        if (instances.containsKey(key)) {
            WeatherSDK existingInstance = instances.get(key);

            if (existingInstance.getMode() != mode) {
                throw new WeatherSDKException(
                        String.format("SDK with this API key already exists in %s mode, but %s was requested. " +
                                        "Use removeInstance() first or use the existing mode.",
                                existingInstance.getMode(), mode));
            }

            LOGGER.log(Level.INFO, "Returning existing WeatherSDK instance for key: {0}",
                    maskApiKey(key));
            return existingInstance;
        }

        // Create new instance
        WeatherSDK newInstance = new WeatherSDK(key, mode);
        instances.put(key, newInstance);

        LOGGER.log(Level.INFO, "Created new WeatherSDK instance for key: {0} in {1} mode",
                new Object[]{maskApiKey(key), mode});

        return newInstance;
    }

    /**
     * Removes SDK instance for specified API key.
     *
     * @param apiKey API key
     * @return true if instance was removed, false if didn't exist
     */
    public static synchronized boolean removeInstance(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        String key = apiKey.trim();
        WeatherSDK instance = instances.remove(key);

        if (instance != null) {
            instance.close();
            LOGGER.log(Level.INFO, "Removed WeatherSDK instance for key: {0}",
                    maskApiKey(key));
            return true;
        }

        return false;
    }

    /**
     * Removes all SDK instances.
     */
    public static synchronized void removeAllInstances() {
        instances.forEach((key, sdk) -> {
            sdk.close();
            LOGGER.log(Level.INFO, "Closed WeatherSDK instance for key: {0}",
                    maskApiKey(key));
        });
        instances.clear();
        LOGGER.log(Level.INFO, "All WeatherSDK instances removed");
    }

    /**
     * Checks if instance exists for specified API key.
     *
     * @param apiKey API key
     * @return true if instance exists
     */
    public static boolean hasInstance(String apiKey) {
        if (apiKey == null) {
            return false;
        }
        return instances.containsKey(apiKey.trim());
    }

    /**
     * Returns number of active SDK instances.
     */
    public static int getInstanceCount() {
        return instances.size();
    }

    /**
     * Masks API key for safe logging.
     *
     * @param apiKey original key
     * @return masked key (shows only first and last 4 characters)
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
