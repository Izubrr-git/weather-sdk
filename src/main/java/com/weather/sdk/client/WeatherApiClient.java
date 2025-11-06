package com.weather.sdk.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.sdk.exception.ApiKeyException;
import com.weather.sdk.exception.CityNotFoundException;
import com.weather.sdk.exception.NetworkException;
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

/**
 * HTTP client for working with OpenWeather API.
 *
 * Uses Jackson for JSON parsing.
 * Handles all API error types with specific exceptions.
 */
public class WeatherApiClient {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final int TIMEOUT_SECONDS = 10;

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates client with specified API key
     *
     * @param apiKey OpenWeather API key
     */
    public WeatherApiClient(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }

        this.apiKey = apiKey.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gets current weather for specified city.
     *
     * @param cityName city name
     * @return weather data
     * @throws ApiKeyException if API key is invalid
     * @throws CityNotFoundException if city not found
     * @throws NetworkException on network errors
     * @throws WeatherSDKException on other errors
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

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                handleErrorResponse(response, cityName);
            }

            return parseResponse(response.body());

        } catch (IOException e) {
            throw new NetworkException("Network error while requesting API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Request was interrupted", e);
        }
    }

    /**
     * Parses JSON API response to WeatherResponse.
     *
     * @param jsonResponse JSON string from API
     * @return WeatherResponse object
     * @throws WeatherSDKException if parsing fails
     */
    private WeatherResponse parseResponse(String jsonResponse) throws WeatherSDKException {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            WeatherResponse response = new WeatherResponse();

            // Parse weather
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
     * Handles HTTP errors with specific exception throwing.
     *
     * @param response HTTP error response
     * @param cityName city name (for error message)
     * @throws ApiKeyException on 401 error
     * @throws CityNotFoundException on 404 error
     * @throws WeatherSDKException on other errors
     */
    private void handleErrorResponse(HttpResponse<String> response, String cityName)
            throws WeatherSDKException {
        int statusCode = response.statusCode();
        String body = response.body();

        String errorMessage = extractErrorMessage(body);

        switch (statusCode) {
            case 401:
                throw new ApiKeyException(
                        "Invalid API key. Please check your credentials at https://openweathermap.org");
            case 404:
                throw new CityNotFoundException("City '" + cityName + "' not found");
            case 429:
                throw new WeatherSDKException(
                        "API rate limit exceeded. Please try again later.");
            case 500:
            case 502:
            case 503:
                throw new NetworkException(
                        "OpenWeather API server error. Please try again later.");
            default:
                throw new WeatherSDKException(
                        String.format("API error (HTTP %d): %s", statusCode, errorMessage));
        }
    }

    /**
     * Extracts error message from JSON response
     *
     * @param body HTTP response body
     * @return error message or "Unknown error"
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
