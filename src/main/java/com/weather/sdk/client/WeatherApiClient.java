package com.weather.sdk.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.weather.sdk.exception.CityNotFoundException;
import com.weather.sdk.exception.NetworkException;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WeatherApiClient {

    private static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final int TIMEOUT_SECONDS = 10;

    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;

    public WeatherApiClient(String apiKey) {
        this.apiKey = apiKey;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.gson = new Gson();
    }

    /**
     * Gets weather data for the specified city.
     *
     * @param cityName : city name
     * @return WeatherData : weather data object
     * @throws WeatherSDKException if an error occurs
     */
    public WeatherData fetchWeather(String cityName) throws WeatherSDKException {
        try {
            String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
            String url = String.format("%s?q=%s&appid=%s", API_BASE_URL, encodedCity, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new CityNotFoundException("City '" + cityName + "' not found");
            }

            if (response.statusCode() == 401) {
                throw new WeatherSDKException("Invalid API key. Check your key at https://openweathermap.org");
            }

            if (response.statusCode() != 200) {
                throw new NetworkException(
                        String.format("API returned an error. Code: %d, Message: %s",
                                response.statusCode(), response.body())
                );
            }

            return parseApiResponse(response.body());

        } catch (IOException e) {
            throw new NetworkException("Network error while requesting API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("The request was aborted", e);
        }
    }

    /**
     * Parses the JSON response from the API and transforms it into our data model.
     *
     * The API response structure differs from the required structure in the task,
     * so we do the mapping manually.
     */
    private WeatherData parseApiResponse(String jsonResponse) throws WeatherSDKException {
        try {
            JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);

            WeatherResponse weatherResponse = new WeatherResponse();

            JsonArray weatherArray = root.getAsJsonArray("weather");
            if (weatherArray != null && weatherArray.size() > 0) {
                JsonObject weatherObj = weatherArray.get(0).getAsJsonObject();
                WeatherResponse.Weather weather = new WeatherResponse.Weather(
                        weatherObj.get("main").getAsString(),
                        weatherObj.get("description").getAsString()
                );
                weatherResponse.setWeather(weather);
            }

            JsonObject main = root.getAsJsonObject("main");
            if (main != null) {
                WeatherResponse.Temperature temperature = new WeatherResponse.Temperature(
                        main.get("temp").getAsDouble(),
                        main.get("feels_like").getAsDouble()
                );
                weatherResponse.setTemperature(temperature);
            }

            // Visibility
            if (root.has("visibility")) {
                weatherResponse.setVisibility(root.get("visibility").getAsInt());
            }

            // Wind
            JsonObject wind = root.getAsJsonObject("wind");
            if (wind != null) {
                WeatherResponse.Wind windData = new WeatherResponse.Wind(wind.get("speed").getAsDouble());
                weatherResponse.setWind(windData);
            }

            // Datetime
            if (root.has("dt")) {
                weatherResponse.setDatetime(root.get("dt").getAsLong());
            }

            // Sys (sunrise/sunset)
            JsonObject sys = root.getAsJsonObject("sys");
            if (sys != null) {
                WeatherResponse.Sys sysData = new WeatherResponse.Sys(
                        sys.get("sunrise").getAsLong(),
                        sys.get("sunset").getAsLong()
                );
                weatherResponse.setSys(sysData);
            }

            // Timezone
            if (root.has("timezone")) {
                weatherResponse.setTimezone(root.get("timezone").getAsInt());
            }

            // Name (city name)
            if (root.has("name")) {
                weatherResponse.setName(root.get("name").getAsString());
            }

            // Wrap WeatherResponse in WeatherData for caching
            return new WeatherData(weatherResponse);

        } catch (Exception e) {
            throw new WeatherSDKException("Error parsing API response: " + e.getMessage(), e);
        }
    }
}