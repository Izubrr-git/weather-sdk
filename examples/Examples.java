package com.weather.sdk.examples;

import com.weather.sdk.WeatherSDK;
import com.weather.sdk.WeatherSDKFactory;
import com.weather.sdk.config.OperationMode;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherResponse;

/**
 * Weather SDK usage examples
 */
public class Examples {

    private static final String API_KEY = "your-api-key-here";

    public static void main(String[] args) {
        // Example 1: Basic usage
        basicUsageExample();

        // Example 2: Using with a Factory
        factoryExample();

        // Example 3: Polling mode
        pollingModeExample();

        // Example 4: Error Handling
        errorHandlingExample();
    }

    /**
     * Example 1: Basic SDK usage in ON_DEMAND mode
     */
    public static void basicUsageExample() {
        System.out.println("=== Example 1: Basic Usage ===\n");

        try (WeatherSDK sdk = new WeatherSDK(API_KEY, OperationMode.ON_DEMAND)) {

            // Request weather for the city
            WeatherResponse weather = sdk.getWeather("London");

            // Output information
            printWeatherInfo(weather);

            // Repeated request - data will be taken from the cache
            System.out.println("\nRetrying request (from cache):");
            WeatherResponse cachedWeather = sdk.getWeather("London");
            printWeatherInfo(cachedWeather);

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Example 2: Using a factory to manage instances
     */
    public static void factoryExample() {
        System.out.println("\n=== Example 2: Using a Factory ===\n");

        try {
            // Create the first instance
            WeatherSDK sdk1 = WeatherSDKFactory.getInstance(API_KEY, OperationMode.ON_DEMAND);
            System.out.println("SDK1 created");

            // Attempt to create a second instance with the same key
            WeatherSDK sdk2 = WeatherSDKFactory.getInstance(API_KEY, OperationMode.ON_DEMAND);
            System.out.println("SDK2 = SDK1: " + (sdk1 == sdk2)); // true

            // Get the weather
            WeatherResponse weather = sdk1.getWeather("Paris");
            printWeatherInfo(weather);

            // Remove the instance
            WeatherSDKFactory.removeInstance(API_KEY);
            System.out.println("SDK removed from factory");

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Example 3: Using POLLING mode
     */
    public static void pollingModeExample() {
        System.out.println("\n=== Example 3: Polling mode ===\n");

        try (WeatherSDK sdk = new WeatherSDK(API_KEY, OperationMode.POLLING)) {

            // Request weather for several cities
            String[] cities = {"Tokyo", "New York", "Moscow"};

            for (String city : cities) {
                WeatherResponse weather = sdk.getWeather(city);
                System.out.println(city + ": " + weather.getTemperature().getTemp() + "K");
            }

            System.out.println("Cities in cache: " + sdk.getCachedCitiesCount());
            System.out.println("In POLLING mode, data is updated automatically every 5 minutes");

            // In a real application, the SDK will continue to run
            // and update data in the background

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Example 4: Handling various errors
     */
    public static void errorHandlingExample() {
        System.out.println("\n=== Example 4: Error Handling ===\n");

        // Error: Invalid API key
        try {
            WeatherSDK sdk = new WeatherSDK("invalid-key", OperationMode.ON_DEMAND);
            sdk.getWeather("London");
        } catch (WeatherSDKException e) {
            System.out.println("Expected error (invalid key): " + e.getMessage());
        }

        // Error: Non-existent city
        try (WeatherSDK sdk = new WeatherSDK(API_KEY, OperationMode.ON_DEMAND)) {
            sdk.getWeather("NonExistentCity12345");
        } catch (WeatherSDKException e) {
            System.out.println("Expected error (city not found): " + e.getMessage());
        }

        // Error: empty city name
        try (WeatherSDK sdk = new WeatherSDK(API_KEY, OperationMode.ON_DEMAND)) {
            sdk.getWeather("");
        } catch (WeatherSDKException e) {
            System.out.println("Expected error (empty name): " + e.getMessage());
        }
    }

    /**
     * Helper method for displaying weather information in a beautiful way
     */
    private static void printWeatherInfo(WeatherResponse weather) {
        System.out.println("City: " + weather.getName());
        System.out.println("Weather: " + weather.getWeather().getMain() +
                " (" + weather.getWeather().getDescription() + ")");
        System.out.println("Temperature: " + weather.getTemperature().getTemp() + "K");
        System.out.println("Feels Like: " + weather.getTemperature().getFeelsLike() + "K");
        System.out.println("Wind Speed: " + weather.getWind().getSpeed() + " m/s");
        System.out.println("Visibility: " + weather.getVisibility() + " m");
    }
}