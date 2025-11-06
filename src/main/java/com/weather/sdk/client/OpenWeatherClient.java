package com.weather.sdk.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class OpenWeatherClient {
    
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final int TIMEOUT_SECONDS = 10;
    
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OpenWeatherClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gets the current weather for the city
     */
    public WeatherResponse getCurrentWeather(String cityName) throws WeatherSDKException {
        try {
            String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
            String url = String.format("%s?q=%s&appid=%s", BASE_URL, encodedCity, apiKey);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                handleErrorResponse(response);
            }
            
            return parseResponse(response.body());
            
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherSDKException("Network error: " + e.getMessage(), e);
        }
    }

    /**
     * Converts the JSON API response to a WeatherResponse
     */
    private WeatherResponse parseResponse(String jsonResponse) throws WeatherSDKException {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            WeatherResponse response = new WeatherResponse();

            // Parse the weather
            JsonNode weatherArray = root.get("weather");
            if (weatherArray != null && weatherArray.isArray() && weatherArray.size() > 0) {
                JsonNode weatherNode = weatherArray.get(0);
                WeatherResponse.Weather weather = new WeatherResponse.Weather(
                    weatherNode.get("main").asText(),
                    weatherNode.get("description").asText()
                );
                response.setWeather(weather);
            }

            // Parse temperature
            JsonNode mainNode = root.get("main");
            if (mainNode != null) {
                WeatherResponse.Temperature temp = new WeatherResponse.Temperature(
                    mainNode.get("temp").asDouble(),
                    mainNode.get("feels_like").asDouble()
                );
                response.setTemperature(temp);
            }
            
            // Parse visibility
            JsonNode visibilityNode = root.get("visibility");
            if (visibilityNode != null) {
                response.setVisibility(visibilityNode.asInt());
            }
            
            // Parse wind
            JsonNode windNode = root.get("wind");
            if (windNode != null) {
                WeatherResponse.Wind wind = new WeatherResponse.Wind(
                    windNode.get("speed").asDouble()
                );
                response.setWind(wind);
            }
            
            // Parse datetime
            JsonNode dtNode = root.get("dt");
            if (dtNode != null) {
                response.setDatetime(dtNode.asLong());
            }
            
            // Parse sys
            JsonNode sysNode = root.get("sys");
            if (sysNode != null) {
                WeatherResponse.Sys sys = new WeatherResponse.Sys(
                    sysNode.get("sunrise").asLong(),
                    sysNode.get("sunset").asLong()
                );
                response.setSys(sys);
            }
            
            // Parse timezone
            JsonNode timezoneNode = root.get("timezone");
            if (timezoneNode != null) {
                response.setTimezone(timezoneNode.asInt());
            }
            
            // Parse name
            JsonNode nameNode = root.get("name");
            if (nameNode != null) {
                response.setName(nameNode.asText());
            }
            
            return response;
            
        } catch (Exception e) {
            throw new WeatherSDKException("Failed to parse API response: " + e.getMessage(), e);
        }
    }

    /**
     * Handles HTTP errors
     */
    private void handleErrorResponse(HttpResponse<String> response) throws WeatherSDKException {
        int statusCode = response.statusCode();
        String body = response.body();
        
        String errorMessage = extractErrorMessage(body);
        
        switch (statusCode) {
            case 401:
                throw new WeatherSDKException("Invalid API key. Please check your credentials.");
            case 404:
                throw new WeatherSDKException("City not found. Please check the city name.");
            case 429:
                throw new WeatherSDKException("API rate limit exceeded. Please try again later.");
            case 500:
            case 502:
            case 503:
                throw new WeatherSDKException("OpenWeather API server error. Please try again later.");
            default:
                throw new WeatherSDKException(String.format("API error (HTTP %d): %s", 
                    statusCode, errorMessage));
        }
    }

    /**
     * Extracts the error message from JSON
     */
    private String extractErrorMessage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode messageNode = root.get("message");
            if (messageNode != null) {
                return messageNode.asText();
            }
        } catch (Exception ignored) {
            // Ignore parsing errors
        }
        return "Unknown error";
    }
}
