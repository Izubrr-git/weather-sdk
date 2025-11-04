package com.weather.sdk.exception;

/**
 * Исключение, выбрасываемое при сетевых ошибках.
 */
public class NetworkException extends WeatherSDKException {
    
    public NetworkException(String message) {
        super(message);
    }
    
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
