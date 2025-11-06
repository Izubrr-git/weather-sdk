package com.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * SDK's main response class with weather data
 */
public class WeatherResponse {
    
    private Weather weather;
    private Temperature temperature;
    private int visibility;
    private Wind wind;
    
    @JsonProperty("datetime")
    private long datetime;
    
    private Sys sys;
    private int timezone;
    private String name;
    
    public WeatherResponse() {}
    
    // Getters и Setters
    
    public Weather getWeather() {
        return weather;
    }
    
    public void setWeather(Weather weather) {
        this.weather = weather;
    }
    
    public Temperature getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }
    
    public int getVisibility() {
        return visibility;
    }
    
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }
    
    public Wind getWind() {
        return wind;
    }
    
    public void setWind(Wind wind) {
        this.wind = wind;
    }
    
    public long getDatetime() {
        return datetime;
    }
    
    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
    
    public Sys getSys() {
        return sys;
    }
    
    public void setSys(Sys sys) {
        this.sys = sys;
    }
    
    public int getTimezone() {
        return timezone;
    }
    
    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherResponse that = (WeatherResponse) o;
        return visibility == that.visibility &&
               datetime == that.datetime &&
               timezone == that.timezone &&
               Objects.equals(weather, that.weather) &&
               Objects.equals(temperature, that.temperature) &&
               Objects.equals(wind, that.wind) &&
               Objects.equals(sys, that.sys) &&
               Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(weather, temperature, visibility, wind, datetime, sys, timezone, name);
    }
    
    @Override
    public String toString() {
        return "WeatherResponse{" +
                "name='" + name + '\'' +
                ", temperature=" + temperature +
                ", weather=" + weather +
                ", visibility=" + visibility +
                ", datetime=" + datetime +
                '}';
    }

    /**
     * Nested class for weather information
     */
    public static class Weather {
        private String main;
        private String description;
        
        public Weather() {}
        
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Weather weather = (Weather) o;
            return Objects.equals(main, weather.main) &&
                   Objects.equals(description, weather.description);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(main, description);
        }
        
        @Override
        public String toString() {
            return main + " (" + description + ")";
        }
    }

    /**
     * Nested class for temperature data
     */
    public static class Temperature {
        private double temp;
        
        @JsonProperty("feels_like")
        private double feelsLike;
        
        public Temperature() {}
        
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
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Temperature that = (Temperature) o;
            return Double.compare(that.temp, temp) == 0 &&
                   Double.compare(that.feelsLike, feelsLike) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(temp, feelsLike);
        }
        
        @Override
        public String toString() {
            return temp + "K (feels like " + feelsLike + "K)";
        }
    }

    /**
     * Nested class for wind data
     */
    public static class Wind {
        private double speed;
        
        public Wind() {}
        
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Wind wind = (Wind) o;
            return Double.compare(wind.speed, speed) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(speed);
        }
        
        @Override
        public String toString() {
            return speed + " m/s";
        }
    }

    /**
     * Nested class for system information
     */
    public static class Sys {
        private long sunrise;
        private long sunset;
        
        public Sys() {}
        
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sys sys = (Sys) o;
            return sunrise == sys.sunrise && sunset == sys.sunset;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(sunrise, sunset);
        }
        
        @Override
        public String toString() {
            return "sunrise=" + sunrise + ", sunset=" + sunset;
        }
    }
}
