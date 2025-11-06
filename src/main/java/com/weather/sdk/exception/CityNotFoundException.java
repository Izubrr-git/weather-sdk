package com.weather.sdk.exception;

public class CityNotFoundException extends WeatherSDKException {
    
    public CityNotFoundException(String message) {
        super(message);
    }
}
