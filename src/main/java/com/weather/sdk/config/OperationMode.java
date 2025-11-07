package com.weather.sdk.config;

/**
 * WeatherSDK operation modes.
 */
public enum OperationMode {
    /**
     * On-demand mode - data is updated only on explicit getWeather() requests.
     * Suitable for apps with infrequent weather queries.
     * <p>
     * Advantages:
     * - Minimal resource consumption
     * - No background threads
     * - Fewer API requests
     */
    ON_DEMAND,

    /**
     * Continuous polling mode - data for all cities in cache is updated
     * automatically every 5 minutes in background.
     * Suitable for apps where minimal latency on weather queries is important.
     * <p>
     * Advantages:
     * - Zero latency on queries (data already in cache)
     * - Always up-to-date data
     * <p>
     * Disadvantages:
     * - Consumes more resources (background thread)
     * - More API requests
     */
    POLLING
}
