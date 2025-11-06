package com.weather.sdk.exception;

public class WeatherSDKException extends Exception {
    
    public WeatherSDKException(String message) {
        super(message);
    }
    
    public WeatherSDKException(String message, Throwable cause) {
        super(message, cause);
    }
}
