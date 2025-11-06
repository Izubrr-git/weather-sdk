# Weather SDK Architecture

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
     ┌───────────▼──┐  ┌──▼──────────────┐
     │ WeatherCache │  │ OpenWeatherClient│
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

### 4. OpenWeatherClient

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
    OpenWeatherClient
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
    │ Every 10 minutes
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
            ├─► Invalid API key
            ├─► City not found
            ├─► Rate limit exceeded
            ├─► Network error
            └─► Server error
```

### Strategy

- All public methods throw `WeatherSDKException`
- Detailed error messages
- Logging via java.util.logging

## Performance

### Optimizations

1. **Caching**: reduces API requests by 90%+
2. **LRU strategy**: O(1) for get/put operations
3. **Polling mode**: zero-latency for user requests
4. **Connection pooling**: built into HttpClient

### Metrics

- **Cache hit rate**: expected >80% for active cities
- **Response time**:
    - Cache: <1ms
    - API: 100-500ms (depends on network)
- **Memory footprint**: ~10KB per city in cache

## Scalability

### Current Limitations

- 10 cities in cache (assignment requirement)
- Single thread for polling

### Extension Possibilities

1. **Configurable cache size**
```java
WeatherSDK(String apiKey, OperationMode mode, int cacheSize)
```

2. **Multiple data sources**
```java
interface WeatherProvider {
    WeatherResponse getWeather(String city);
}
```

3. **Configurable polling intervals**
```java
PollingConfig config = new PollingConfig()
    .interval(5, TimeUnit.MINUTES)
    .maxConcurrentUpdates(3);
```

4. **Persistent cache**
```java
WeatherCache extends PersistentCache<String, WeatherData>
```

## Security

### Protection Measures

1. **API Key hidden in logs**: masking in WeatherSDKFactory
2. **Input data validation**: null-checks, empty-checks
3. **Timeout protection**: 10 seconds per request
4. **Rate limiting awareness**: handling 429 errors

### Recommendations

- Store API keys in environment variables
- Use HTTPS (default)
- Don't log sensitive data

## Testing

### Unit Tests

- WeatherSDKTest: basic functionality
- WeatherSDKFactoryTest: instance management
- WeatherCacheTest: caching and LRU
- OpenWeatherClientTest: HTTP client (with mock)

### Integration Tests

- Real requests to OpenWeather API
- Polling mode testing
- Cache stress testing

## CI/CD Pipeline (Recommendations)

```yaml
build:
  - mvn clean compile
  
test:
  - mvn test
  - mvn jacoco:report (coverage > 80%)
  
quality:
  - mvn checkstyle:check
  - mvn pmd:check
  - SonarQube analysis
  
package:
  - mvn package
  - Generate javadoc
  
deploy:
  - Maven Central
  - GitHub Releases
```

## Dependencies

### Direct

- **Jackson 2.15.2**: JSON serialization
- **JUnit 5.10.0**: testing (scope: test)

### Implicit

- Java 11 HttpClient (built-in)
- java.util.concurrent (built-in)
- java.util.logging (built-in)

**Advantages of minimal dependencies**:
- Small artifact size
- Fewer version conflicts
- Fast installation