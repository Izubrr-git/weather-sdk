package com.weather.sdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Temperature information.
 * Temperature in Kelvin.
 */
public class Temperature {
    
    @SerializedName("temp")
    private double temp;
    
    @SerializedName("feels_like")
    private double feelsLike;

    public Temperature() {
    }

    public Temperature(double temp, double feelsLike) {
        this.temp = temp;
        this.feelsLike = feelsLike;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    /**
     * Converts temperature from Kelvin to Celsius.
     */
    public double getTempCelsius() {
        return temp - 273.15;
    }

    /**
     * Converts perceived temperature from Kelvin to Celsius.
     */
    public double getFeelsLikeCelsius() {
        return feelsLike - 273.15;
    }

    @Override
    public String toString() {
        return "Temperature{" +
                "temp=" + temp +
                ", feelsLike=" + feelsLike +
                '}';
    }
}
