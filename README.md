# Weather SDK

A lightweight Java SDK for accessing the OpenWeather API with smart caching and flexible operation modes.

## Quick Start

```java
// Create SDK
WeatherSDK sdk = new WeatherSDK("your-api-key", OperationMode.ON_DEMAND);

// Get weather
WeatherResponse weather = sdk.getWeather("London");
System.out.println(weather.getName() + ": " + 
                   weather.getTemperature().getTempCelsius() + "°C");

// Clean up
sdk.close();
```

## Examples

For detailed usage examples, see:
- **[examples/Examples.java](examples/Examples.java)** - Basic usage patterns and common scenarios
- **[examples/WeatherSDKExample.java](examples/WeatherSDKExample.java)** - Comprehensive examples covering all SDK features

## Component Overview

```
┌─────────────────────────────────────────────────────┐
│                       User                          │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────▼────────────┐
         │  WeatherSDKFactory     │  ← Singleton for instance management
         │  (Optional)            │
         └───────────┬────────────┘
                     │
         ┌───────────▼────────────┐
         │     WeatherSDK         │  ← Main class
         │  - ON_DEMAND           │
         │  - POLLING             │
         └───────┬────────┬───────┘
                 │        │
     ┌───────────▼──┐  ┌──▼───────────────┐
     │ WeatherCache │  │ WeatherApiClient │
     │ (LRU, 10max) │  │ (HTTP)           │
     └──────────────┘  └──┬───────────────┘
                          │
              ┌───────────▼───────────┐
              │  OpenWeather API      │
              │  (External Service)   │
              └───────────────────────┘
```

## Components

### 1. WeatherSDK (Core)

**Purpose**: Main SDK class providing API for working with weather.

**Responsibilities**:
- Managing SDK lifecycle
- Coordination between cache and HTTP client
- Implementation of two operation modes (ON_DEMAND, POLLING)
- Managing background updates (in POLLING mode)

**Key Methods**:
```java
WeatherResponse getWeather(String cityName)
void clearCache()
void close()
```

**Patterns**:
- AutoCloseable for resource management
- Strategy (two operation modes)

### 2. WeatherSDKFactory

**Purpose**: Managing creation and lifecycle of SDK instances.

**Responsibilities**:
- Guarantee single instance per API key (Singleton per key)
- Preventing memory leaks
- Centralized resource management

**Key Methods**:
```java
static WeatherSDK getInstance(String apiKey, OperationMode mode)
static boolean removeInstance(String apiKey)
```

**Patterns**:
- Factory
- Registry Pattern
- Thread-safe with ConcurrentHashMap

### 3. WeatherCache

**Purpose**: Caching weather data with automatic invalidation.

**Characteristics**:
- **Strategy**: LRU (Least Recently Used)
- **Size**: 10 cities maximum
- **TTL**: 10 minutes
- **Thread-safe**: synchronized methods

**Implementation**:
```java
LinkedHashMap with accessOrder=true + overridden removeEldestEntry()
```

### 4. WeatherApiClient

**Purpose**: HTTP client for interaction with OpenWeather API.

**Characteristics**:
- Java 11 HttpClient
- Timeout: 10 seconds
- Detailed error handling
- URL encoding for city names

**Error Handling**:
- 401: Invalid API key
- 404: City not found
- 429: Rate limit exceeded
- 5xx: Server errors

### 5. Model (WeatherResponse, WeatherData)

**WeatherResponse**: DTO for weather data (matches assignment format).

**WeatherData**: Wrapper with timestamp for cache validity validation.

## Data Flows

### On-Demand Mode

```
User
    │
    │ getWeather("London")
    ▼
WeatherSDK
    │
    │ 1. Check cache
    ▼
WeatherCache
    │
    ├─► [Cache valid] ──► Return from cache
    │
    └─► [Cache expired/missing]
         │
         ▼
    WeatherApiClient
         │
         │ HTTP GET
         ▼
    OpenWeather API
         │
         │ JSON Response
         ▼
    WeatherSDK
         │
         │ Save to cache
         ▼
    Return to user
```

### Polling Mode

```
SDK Initialization
    │
    │ Start ScheduledExecutorService
    ▼
Background Thread
    │
    │ Every 5 minutes
    │
    ▼
For each city in cache:
    │
    │ HTTP GET
    ▼
OpenWeather API
    │
    │ Update cache
    ▼
Done

In parallel:
User ──► getWeather() ──► Instant response from cache
```

## Thread Safety

### Synchronization

1. **WeatherCache**: all methods synchronized
2. **WeatherSDKFactory**: getInstance() and remove methods synchronized
3. **ScheduledExecutorService**: daemon thread for polling

### Shutdown Strategy

```java
close() → shutdown scheduler → await termination → force shutdown if needed
```

## Error Handling

### Exception Hierarchy

```
Exception
    │
    └─► WeatherSDKException
            │
            ├─► ApiKeyException (Invalid API key)
            ├─► CityNotFoundException (City not found)
            ├─► NetworkException (Network error)
            └─► (Rate limit exceeded, Server error, etc.)
```

### Strategy

- All public methods throw `WeatherSDKException`
- Detailed error messages
- Logging via java.util.logging
