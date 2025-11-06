package com.weather.sdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Weather information.
 */
public class Weather {
    
    @SerializedName("main")
    private String main;
    
    @SerializedName("description")
    private String description;

    public Weather() {
    }

    public Weather(String main, String description) {
        this.main = main;
        this.description = description;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "main='" + main + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
