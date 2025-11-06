package com.weather.sdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Wind information.
 */
public class Wind {
    
    @SerializedName("speed")
    private double speed;

    public Wind() {
    }

    public Wind(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "Wind{" +
                "speed=" + speed +
                '}';
    }
}
