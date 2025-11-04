package com.weather.sdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Системная информация (восход и закат солнца).
 */
public class Sys {
    
    @SerializedName("sunrise")
    private long sunrise;
    
    @SerializedName("sunset")
    private long sunset;

    public Sys() {
    }

    public Sys(long sunrise, long sunset) {
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    @Override
    public String toString() {
        return "Sys{" +
                "sunrise=" + sunrise +
                ", sunset=" + sunset +
                '}';
    }
}
