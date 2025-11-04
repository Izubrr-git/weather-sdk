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

/**
 * HTTP клиент для взаимодействия с OpenWeatherMap API.
 * 
 * Использует современный java.net.http.HttpClient (Java 11+).
 */
public class WeatherApiClient {
    
    private static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final int TIMEOUT_SECONDS = 10;
    
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;
    
    public WeatherApiClient(String apiKey) {
        this.apiKey = apiKey;
        
        // Настраиваем HttpClient с таймаутом и редиректами
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        
        this.gson = new Gson();
    }
    
    /**
     * Получает данные о погоде для указанного города.
     * 
     * @param cityName название города
     * @return WeatherData объект с данными о погоде
     * @throws WeatherSDKException если произошла ошибка
     */
    public WeatherData fetchWeather(String cityName) throws WeatherSDKException {
        try {
            // Формируем URL с параметрами
            String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
            String url = String.format("%s?q=%s&appid=%s", API_BASE_URL, encodedCity, apiKey);
            
            // Создаем HTTP запрос
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();
            
            // Отправляем запрос
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Проверяем статус ответа
            if (response.statusCode() == 404) {
                throw new CityNotFoundException("Город '" + cityName + "' не найден");
            }
            
            if (response.statusCode() == 401) {
                throw new WeatherSDKException("Неверный API ключ. Проверьте ваш ключ на https://openweathermap.org");
            }
            
            if (response.statusCode() != 200) {
                throw new NetworkException(
                    String.format("API вернул ошибку. Код: %d, Сообщение: %s", 
                                response.statusCode(), response.body())
                );
            }
            
            // Парсим ответ и преобразуем в нашу модель
            return parseApiResponse(response.body());
            
        } catch (IOException e) {
            throw new NetworkException("Ошибка сети при запросе к API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Запрос был прерван", e);
        }
    }
    
    /**
     * Парсит JSON ответ от API и преобразует его в нашу модель данных.
     * 
     * Структура ответа API отличается от требуемой структуры в задании,
     * поэтому мы делаем маппинг вручную.
     */
    private WeatherData parseApiResponse(String jsonResponse) throws WeatherSDKException {
        try {
            JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);
            
            WeatherData weatherData = new WeatherData();
            
            // Парсим weather (берем первый элемент из массива)
            JsonArray weatherArray = root.getAsJsonArray("weather");
            if (weatherArray != null && weatherArray.size() > 0) {
                JsonObject weatherObj = weatherArray.get(0).getAsJsonObject();
                Weather weather = new Weather(
                    weatherObj.get("main").getAsString(),
                    weatherObj.get("description").getAsString()
                );
                weatherData.setWeather(weather);
            }
            
            // Парсим temperature (из "main")
            JsonObject main = root.getAsJsonObject("main");
            if (main != null) {
                Temperature temperature = new Temperature(
                    main.get("temp").getAsDouble(),
                    main.get("feels_like").getAsDouble()
                );
                weatherData.setTemperature(temperature);
            }
            
            // Visibility
            if (root.has("visibility")) {
                weatherData.setVisibility(root.get("visibility").getAsInt());
            }
            
            // Wind
            JsonObject wind = root.getAsJsonObject("wind");
            if (wind != null) {
                Wind windData = new Wind(wind.get("speed").getAsDouble());
                weatherData.setWind(windData);
            }
            
            // Datetime
            if (root.has("dt")) {
                weatherData.setDatetime(root.get("dt").getAsLong());
            }
            
            // Sys (sunrise/sunset)
            JsonObject sys = root.getAsJsonObject("sys");
            if (sys != null) {
                Sys sysData = new Sys(
                    sys.get("sunrise").getAsLong(),
                    sys.get("sunset").getAsLong()
                );
                weatherData.setSys(sysData);
            }
            
            // Timezone
            if (root.has("timezone")) {
                weatherData.setTimezone(root.get("timezone").getAsInt());
            }
            
            // Name (название города)
            if (root.has("name")) {
                weatherData.setName(root.get("name").getAsString());
            }
            
            return weatherData;
            
        } catch (Exception e) {
            throw new WeatherSDKException("Ошибка при парсинге ответа API: " + e.getMessage(), e);
        }
    }
}
