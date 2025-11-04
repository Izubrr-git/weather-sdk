package com.weather.sdk.exception;

/**
 * Исключение, выбрасываемое при проблемах с API ключом.
 */
public class ApiKeyException extends WeatherSDKException {
    
    public ApiKeyException(String message) {
        super(message);
    }
}
