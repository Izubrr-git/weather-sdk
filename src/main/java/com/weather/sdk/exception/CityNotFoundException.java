package com.weather.sdk.exception;

/**
 * Исключение, выбрасываемое когда город не найден в API.
 */
public class CityNotFoundException extends WeatherSDKException {
    
    public CityNotFoundException(String message) {
        super(message);
    }
}
