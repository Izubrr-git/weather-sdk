package com.weather.sdk.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Обёртка для кэшированных данных о погоде с проверкой актуальности
 */
public class WeatherData {
    
    private static final long CACHE_VALIDITY_MINUTES = 10;
    
    private final WeatherResponse weatherResponse;
    private final Instant timestamp;
    
    /**
     * Создаёт обёртку с текущим временем
     */
    public WeatherData(WeatherResponse weatherResponse) {
        this.weatherResponse = weatherResponse;
        this.timestamp = Instant.now();
    }
    
    /**
     * Проверяет, актуальны ли данные (не прошло ли 10 минут)
     */
    public boolean isValid() {
        Instant now = Instant.now();
        long minutesPassed = ChronoUnit.MINUTES.between(timestamp, now);
        return minutesPassed < CACHE_VALIDITY_MINUTES;
    }
    
    public WeatherResponse getWeatherResponse() {
        return weatherResponse;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
}
