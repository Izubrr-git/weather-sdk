package com.weather.sdk.config;

/**
 * WeatherSDK operating modes.
 */
public enum SDKMode {
    /**
     * On-demand mode - data is updated only when explicit getWeather() requests are made.
     * Suitable for apps with infrequent weather requests.
     *
     * Advantages:
     * - Minimal resource consumption
     * - No background threads
     * - Fewer API requests
     */
    ON_DEMAND,

    /**
     * Continuous refresh mode - data for all cities in the cache is updated
     * automatically every 5 minutes in the background.
     * Suitable for apps where minimal latency when querying weather is important.
     *
     * Advantages:
     * - Zero latency when querying (data is already in the cache)
     * - Always up-to-date data
     *
     * Disadvantages:
     * - Consumes more resources (background thread)
     * - More API requests
     */
    POLLING
}
