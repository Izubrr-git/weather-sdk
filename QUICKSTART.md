# Quick Start with Weather SDK ⚡

This quick guide will help you get started with Weather SDK in 5 minutes!

## Step 1: Get an API Key 🔑

1. Register at [OpenWeatherMap](https://openweathermap.org/appid)
2. Go to the **API keys** section
3. Copy your API key
4. ⏱️ Wait 10-15 minutes for the key to activate

## Step 2: Add Dependency 📦

### Maven

```xml
<dependency>
    <groupId>com.weather</groupId>
    <artifactId>weather-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.weather:weather-sdk:1.0.0'
```

### Or Use JAR Directly

```bash
# Download the JAR file and add to classpath
java -cp weather-sdk-1.0.0.jar:. YourApp
```

## Step 3: Write Your First Code 🚀

### Simplest Example

```java
import com.weather.sdk.*;
import com.weather.sdk.config.OperationMode;
import com.weather.sdk.model.WeatherData;

public class QuickStart {
    public static void main(String[] args) {
        String apiKey = "your_api_key_here";

        try {
            // 1. Create SDK
            WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, OperationMode.ON_DEMAND);

            // 2. Get weather
            WeatherData weather = sdk.getWeather("Moscow");

            // 3. Use the data
            System.out.println("🌍 City: " + weather.getName());
            System.out.println("🌡️  Temperature: " +
                    String.format("%.1f°C", weather.getTemperature().getTempCelsius()));
            System.out.println("☁️  Weather: " + weather.getWeather().getDescription());

            // 4. Close SDK
            WeatherSDKFactory.removeInstance(apiKey);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Run It!

```bash
javac QuickStart.java
java QuickStart
```

**Expected Output:**
```
🌍 City: Moscow
🌡️  Temperature: -5.2°C
☁️  Weather: scattered clouds
```

## Step 4: Explore Main Features 💡

### Working with Multiple Cities

```java
WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);

String[] cities = {"Moscow", "London", "Paris", "Tokyo"};
for (String city : cities) {
    try {
        WeatherData weather = sdk.getWeather(city);
        System.out.printf("%s: %.1f°C%n", 
            weather.getName(), 
            weather.getTemperature().getTempCelsius()
        );
    } catch (Exception e) {
        System.err.println(city + ": Error - " + e.getMessage());
    }
}

WeatherSDKFactory.removeInstance(apiKey);
```

### POLLING Mode (Auto-update)

```java
// Create SDK in POLLING mode
WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.POLLING);

// First request loads data
sdk.getWeather("London");
sdk.getWeather("Paris");

// Data will automatically update every 5 minutes!
// All subsequent requests are instant (from cache)

Thread.sleep(60000); // wait a minute

// Instant response from cache
WeatherData weather = sdk.getWeather("London");

WeatherSDKFactory.removeInstance(apiKey);
```

### Try-with-resources (Recommended)

```java
try (WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND)) {
    WeatherData weather = sdk.getWeather("Berlin");
    System.out.println(weather.getName() + ": " + 
        weather.getTemperature().getTempCelsius() + "°C");
} // SDK will close automatically

WeatherSDKFactory.removeInstance(apiKey);
```

## Step 5: Handle Errors Properly 🛡️

```java
try {
    WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);
    WeatherData weather = sdk.getWeather("Moscow");
    
    // Your code here
    
} catch (CityNotFoundException e) {
    System.err.println("❌ City not found!");
} catch (NetworkException e) {
    System.err.println("❌ Internet connection problem!");
} catch (ApiKeyException e) {
    System.err.println("❌ API key problem!");
} catch (WeatherSDKException e) {
    System.err.println("❌ Error: " + e.getMessage());
}
```

## 🎯 What's Next?

### Study the Documentation
- 📖 [README.md](README.md) - Full documentation
- 🏗️ [ARCHITECTURE.md](ARCHITECTURE.md) - SDK architecture
- 💻 [Examples](../examples/) - More code examples

### Key Concepts

1. **Two Operating Modes:**
    - `ON_DEMAND` - requests on demand
    - `POLLING` - automatic updates

2. **Automatic Caching:**
    - Data is valid for 10 minutes
    - Maximum 10 cities in cache

3. **Factory Pattern:**
    - One SDK per API key
    - Use `WeatherSDKFactory` for creation

4. **Error Handling:**
    - Specific exceptions for different errors
    - Always handle exceptions

### Useful Tips 💡

✅ **Do:**
- Use try-with-resources
- Handle exceptions specifically
- Close SDK after use
- Choose the right mode for your task

❌ **Don't:**
- Don't create many instances for one key
- Don't ignore exceptions
- Don't forget to remove instances from factory
- Don't make too frequent requests (API limit)

## 🆘 Problems?

### API Key Doesn't Work
```
Error 401: Invalid API key
```
**Solution:** Wait 10-15 minutes after registration for key activation.

### City Not Found
```
CityNotFoundException: City 'Moscw' not found
```
**Solution:** Check the spelling. Use English names.

### Network Error
```
NetworkException: Connection timeout
```
**Solution:** Check internet connection and availability of api.openweathermap.org.

---

Congratulations! 🎉 You're ready to use Weather SDK in your projects!