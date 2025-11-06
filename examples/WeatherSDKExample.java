package examples;

import com.weather.sdk.WeatherSDK;
import com.weather.sdk.WeatherSDKFactory;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherData;

/**
 * WeatherSDK usage examples.
 *
 * ⚠️ Replace "YOUR_API_KEY" with your actual API key from https://openweathermap.org
 */
public class WeatherSDKExample {
    
    private static final String API_KEY = "YOUR_API_KEY";
    
    public static void main(String[] args) {
        System.out.println("=== Weather SDK Usage Examples ===\n");

        // Example 1: Basic usage in ON_DEMAND mode
        example1_BasicUsage();

        // Example 2: Usage in POLLING mode
        example2_PollingMode();

        // Example 3: Caching demo
        example3_CachingDemo();

        // Example 4: Error handling
        example4_ErrorHandling();

        // Example 5: Working with multiple SDK instances
        example5_MultipleInstances();

        // Example 6: Try-with-resources (automatic closure)
        example6_TryWithResources();
    }

    /**
     * Example 1: Basic usage in ON_DEMAND mode.
     */
    private static void example1_BasicUsage() {
        System.out.println("📍 Example 1: Basic usage\n");

        try {
            // Create an SDK instance in ON_DEMAND mode
            WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.ON_DEMAND);

            // Get the weather for Moscow
            WeatherData weather = sdk.getWeather("Moscow");

            // Display information
            System.out.println("City: " + weather.getName());
            System.out.println("Weather: " + weather.getWeather().getMain() +
                    " (" + weather.getWeather().getDescription() + ")");
            System.out.println("Temperature: " +
                    String.format("%.1f°C", weather.getTemperature().getTempCelsius()));
            System.out.println("Feels Like: " +
                    String.format("%.1f°C", weather.getTemperature().getFeelsLikeCelsius()));
            System.out.println("Wind: " + weather.getWind().getSpeed() + " m/s");
            System.out.println("Visibility: " + weather.getVisibility() + " m\n");

            // Remove the instance
            WeatherSDKFactory.removeInstance(API_KEY);

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Example 2: Using POLLING mode.
     * In this mode, data is updated automatically every 5 minutes.
     */
    private static void example2_PollingMode() {
        System.out.println("📍 Example 2: POLLING mode\n");

        try {
            // Create SDK in POLLING mode
            WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.POLLING);

            // First request - data is loaded from the API
            System.out.println("First request (download from API):");
            WeatherData weather1 = sdk.getWeather("London");
            printWeatherShort(weather1);

            // Add more cities
            sdk.getWeather("Paris");
            sdk.getWeather("Berlin");

            System.out.println("Cacheed cities: " + sdk.getCacheSize());
            System.out.println("Polling will automatically update data every 5 minutes\n");

            // Subsequent requests will return data from the cache immediately
            System.out.println("Repeat request (from cache, instantly):");
            WeatherData weather2 = sdk.getWeather("London");
            printWeatherShort(weather2);

            WeatherSDKFactory.removeInstance(API_KEY);

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Example 3: Cache demonstration.
     */
    private static void example3_CachingDemo() {
        System.out.println("📍 Example 3: Caching (ON_DEMAND mode)\n");

        try {
            WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.ON_DEMAND);

            // First request - goes to the API
            System.out.println("First request to Tokyo (from API):");
            long start1 = System.currentTimeMillis();
            sdk.getWeather("Tokyo");
            long time1 = System.currentTimeMillis() - start1;
            System.out.println("Time: " + time1 + " ms\n");

            // Second request - from cache (should be faster)
            System.out.println("Second request for Tokyo (from cache):");
            long start2 = System.currentTimeMillis();
            sdk.getWeather("Tokyo");
            long time2 = System.currentTimeMillis() - start2;
            System.out.println("Time: " + time2 + " ms");
            System.out.println("Cache worked! Speedup of " + (time1 / Math.max(time2, 1)) + " times\n");

            // Demonstration of the cache limit (10 cities)
            System.out.println("Adding 10 cities to the cache:");
            String[] cities = {"New York", "Los Angeles", "Chicago", "Houston",
                    "Phoenix", "Philadelphia", "San Antonio", "San Diego",
                    "Dallas", "San Jose"};

            for (String city : cities) {
                sdk.getWeather(city);
                System.out.println(" Added: " + city + " (in cache: " + sdk.getCacheSize() + ")");
            }

            System.out.println("\nAttempt to add 11th city:");
            sdk.getWeather("Austin");
            System.out.println(" Austin added, oldest city removed from cache");
            System.out.println(" Remaining in cache: " + sdk.getCacheSize() + " cities\n");

            WeatherSDKFactory.removeInstance(API_KEY);

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Example 4: Handling different types of errors.
     */
    private static void example4_ErrorHandling() {
        System.out.println("📍 Example 4: Error Handling\n");

        try {
            WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.ON_DEMAND);

            // Attempting to get weather for a non-existent city
            System.out.println("Request for a non-existent city:");
            try {
                sdk.getWeather("NonExistentCityXYZ123");
            } catch (WeatherSDKException e) {
                System.out.println(" ❌ Expected error: " + e.getMessage());
            }

            // Attempt to pass an empty name
            System.out.println("\nRequest with an empty name:");
            try {
                sdk.getWeather("");
            } catch (WeatherSDKException e) {
                System.out.println(" ❌ Expected error: " + e.getMessage());
            }

            System.out.println();
            WeatherSDKFactory.removeInstance(API_KEY);

        } catch (WeatherSDKException e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Example 5: Working with multiple API keys.
     */
    private static void example5_MultipleInstances() {
        System.out.println("📍 Example 5: Multiple SDK instances\n");

        String apiKey1 = API_KEY;
        String apiKey2 = "ANOTHER_API_KEY"; // For demonstration

        try {
            // Create the first instance
            WeatherSDK sdk1 = WeatherSDKFactory.getInstance(apiKey1, SDKMode.ON_DEMAND);
            System.out.println("✅ SDK with the first API key created");

            // Attempt to create a second instance with the same key
            WeatherSDK sdk1_duplicate = WeatherSDKFactory.getInstance(apiKey1, SDKMode.ON_DEMAND);
            System.out.println("✅ Received an existing SDK (same object): " +
                    (sdk1 == sdk1_duplicate));

            // Attempting to create an SDK with the same key but a different mode
            System.out.println("\nAttempt to create an SDK with the same key but POLLING mode:");
            try {
                WeatherSDK sdk1_different_mode = WeatherSDKFactory.getInstance(apiKey1, SDKMode.POLLING);
            } catch (WeatherSDKException e) {
                System.out.println(" ❌ Expected error: " + e.getMessage());
            }

            System.out.println("\nTotal active instances: " + WeatherSDKFactory.getInstanceCount());

            // Remove all instances
            WeatherSDKFactory.removeAllInstances();
            System.out.println("After removing all instances: " + WeatherSDKFactory.getInstanceCount() + "\n");

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Example 6: Try-with-resources for automatic resource release.
     */
    private static void example6_TryWithResources() {
        System.out.println("📍 Example 6: Try-with-resources (recommended approach)\n");

        try (WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.POLLING)) {

            System.out.println("The SDK has been created and will be automatically closed");

            WeatherData weather = sdk.getWeather("Sydney");
            printWeatherShort(weather);

            System.out.println("When exiting the try block, the SDK will automatically close\n");

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // The SDK is already closed, clearing it from the factory
        WeatherSDKFactory.removeInstance(API_KEY);
    }

    /**
     * Helper method for displaying brief weather information.
     */
    private static void printWeatherShort(WeatherData weather) {
        System.out.println("  " + weather.getName() + ": " + 
                         weather.getWeather().getMain() + ", " +
                         String.format("%.1f°C", weather.getTemperature().getTempCelsius()));
    }
}
