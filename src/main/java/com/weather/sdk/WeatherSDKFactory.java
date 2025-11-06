package com.weather.sdk;

import com.weather.sdk.exception.WeatherSDKException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating and managing WeatherSDK instances
 *
 * Ensures that only one SDK instance exists for each unique API key.
 * This prevents duplicate polling threads and optimizes resource usage.
 *
 * Example usage:
 * <pre>
 * WeatherSDK sdk1 = WeatherSDKFactory.getInstance("key1", OperationMode.POLLING);
 * WeatherSDK sdk2 = WeatherSDKFactory.getInstance("key1", OperationMode.POLLING); // returns sdk1
 *
 * WeatherSDKFactory.removeInstance("key1"); // removes and closes the SDK
 * </pre>
 */
public class WeatherSDKFactory {
    
    private static final Logger LOGGER = Logger.getLogger(WeatherSDKFactory.class.getName());

    private static final Map<String, WeatherSDK> instances = new ConcurrentHashMap<>();

    private WeatherSDKFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Gets or creates a WeatherSDK instance for the specified API key.
     *
     * @param apiKey : OpenWeather API key
     * @param mode : SDK mode
     * @return WeatherSDK instance
     * @throws WeatherSDKException if SDK creation failed
     */
    public static synchronized WeatherSDK getInstance(String apiKey, WeatherSDK.OperationMode mode) 
            throws WeatherSDKException {
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSDKException("API key cannot be null or empty");
        }
        
        String key = apiKey.trim();

        if (instances.containsKey(key)) {
            WeatherSDK existingInstance = instances.get(key);
            LOGGER.log(Level.INFO, "Returning existing WeatherSDK instance for key: {0}", 
                      maskApiKey(key));
            return existingInstance;
        }

        WeatherSDK newInstance = new WeatherSDK(key, mode);
        instances.put(key, newInstance);
        
        LOGGER.log(Level.INFO, "Created new WeatherSDK instance for key: {0}", 
                  maskApiKey(key));
        
        return newInstance;
    }

    /**
     * Deletes the SDK instance for the specified API key.
     *
     * @param apiKey API key
     * @return true if the instance was deleted, false if it did not exist.
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
     * Removes all SDK instances
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
     * Checks if an instance exists for the specified API key
     */
    public static boolean hasInstance(String apiKey) {
        if (apiKey == null) {
            return false;
        }
        return instances.containsKey(apiKey.trim());
    }

    /**
     * Returns the number of active SDK instances
     */
    public static int getInstanceCount() {
        return instances.size();
    }

    /**
     * Masks the API key for secure logging
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
