package com.weather.sdk;

import com.weather.sdk.model.WeatherResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeatherResponse and its nested classes
 *
 * Tests model structure and temperature conversion without API access
 */
class WeatherResponseTest {

    @Test
    void testWeatherResponseCreation() {
        WeatherResponse response = new WeatherResponse();
        response.setName("London");

        assertEquals("London", response.getName());
    }

    @Test
    void testTemperatureConversion() {
        WeatherResponse.Temperature temp = new WeatherResponse.Temperature(273.15, 271.0);

        // Test Kelvin values
        assertEquals(273.15, temp.getTemp());
        assertEquals(271.0, temp.getFeelsLike());

        // Test Celsius conversion
        assertEquals(0.0, temp.getTempCelsius(), 0.01);
        assertEquals(-2.15, temp.getFeelsLikeCelsius(), 0.01);
    }

    @Test
    void testTemperatureToString() {
        WeatherResponse.Temperature temp = new WeatherResponse.Temperature(290.15, 288.0);

        String str = temp.toString();

        assertTrue(str.contains("290") || str.contains("temp=290.15"),
                "Temperature string should contain temperature value: " + str);
        assertTrue(str.contains("288"),
                "Temperature string should contain feels_like value: " + str);
    }

    @Test
    void testWeatherCreation() {
        WeatherResponse.Weather weather = new WeatherResponse.Weather("Clear", "clear sky");

        assertEquals("Clear", weather.getMain());
        assertEquals("clear sky", weather.getDescription());
    }

    @Test
    void testWeatherToString() {
        WeatherResponse.Weather weather = new WeatherResponse.Weather("Clouds", "scattered clouds");

        String str = weather.toString();

        assertTrue(str.contains("Clouds"), "Weather string should contain main value: " + str);
        assertTrue(str.contains("scattered clouds"), "Weather string should contain description: " + str);
    }

    @Test
    void testWindCreation() {
        WeatherResponse.Wind wind = new WeatherResponse.Wind(5.5);

        assertEquals(5.5, wind.getSpeed());
    }

    @Test
    void testWindToString() {
        WeatherResponse.Wind wind = new WeatherResponse.Wind(7.3);

        String str = wind.toString();

        // Wind toString может быть "7.3 m/s" или "Wind{speed=7.3}"
        assertTrue(str.contains("7.3"), "Wind string should contain speed value: " + str);
    }

    @Test
    void testSysCreation() {
        WeatherResponse.Sys sys = new WeatherResponse.Sys(1234567890L, 1234567900L);

        assertEquals(1234567890L, sys.getSunrise());
        assertEquals(1234567900L, sys.getSunset());
    }

    @Test
    void testCompleteWeatherResponse() {
        WeatherResponse response = new WeatherResponse();

        response.setName("London");

        WeatherResponse.Temperature temp = new WeatherResponse.Temperature(290.15, 288.0);
        response.setTemperature(temp);

        WeatherResponse.Weather weather = new WeatherResponse.Weather("Clear", "clear sky");
        response.setWeather(weather);

        WeatherResponse.Wind wind = new WeatherResponse.Wind(5.5);
        response.setWind(wind);

        response.setVisibility(10000);
        response.setDatetime(1234567890L);

        WeatherResponse.Sys sys = new WeatherResponse.Sys(1234567800L, 1234567900L);
        response.setSys(sys);

        response.setTimezone(3600);

        // Verify all fields
        assertEquals("London", response.getName());
        assertEquals(290.15, response.getTemperature().getTemp());
        assertEquals("Clear", response.getWeather().getMain());
        assertEquals(5.5, response.getWind().getSpeed());
        assertEquals(10000, response.getVisibility());
        assertEquals(1234567890L, response.getDatetime());
        assertEquals(3600, response.getTimezone());
    }

    @Test
    void testWeatherResponseToString() {
        WeatherResponse response = new WeatherResponse();
        response.setName("Paris");

        WeatherResponse.Temperature temp = new WeatherResponse.Temperature(285.0, 283.0);
        response.setTemperature(temp);

        WeatherResponse.Weather weather = new WeatherResponse.Weather("Clear", "clear sky");
        response.setWeather(weather);

        String str = response.toString();

        assertTrue(str.contains("Paris"), "Response string should contain city name: " + str);

        assertTrue(str.contains("285") || str.contains("temperature"),
                "Response string should contain temperature info: " + str);
    }

    @Test
    void testEqualsAndHashCode() {
        WeatherResponse.Temperature temp1 = new WeatherResponse.Temperature(290.15, 288.0);
        WeatherResponse.Temperature temp2 = new WeatherResponse.Temperature(290.15, 288.0);
        WeatherResponse.Temperature temp3 = new WeatherResponse.Temperature(291.0, 289.0);

        assertEquals(temp1, temp2);
        assertNotEquals(temp1, temp3);

        assertEquals(temp1.hashCode(), temp2.hashCode());
        assertNotEquals(temp1.hashCode(), temp3.hashCode());
    }

    @Test
    void testTemperatureEquality() {
        WeatherResponse.Temperature temp1 = new WeatherResponse.Temperature(290.15, 288.0);
        WeatherResponse.Temperature temp2 = new WeatherResponse.Temperature(290.15, 288.0);

        // Test reflexive
        assertEquals(temp1, temp1);

        // Test symmetric
        assertEquals(temp1, temp2);
        assertEquals(temp2, temp1);

        // Test null
        assertNotEquals(temp1, null);
    }

    @Test
    void testWeatherEquality() {
        WeatherResponse.Weather weather1 = new WeatherResponse.Weather("Clear", "clear sky");
        WeatherResponse.Weather weather2 = new WeatherResponse.Weather("Clear", "clear sky");
        WeatherResponse.Weather weather3 = new WeatherResponse.Weather("Clouds", "cloudy");

        assertEquals(weather1, weather2);
        assertNotEquals(weather1, weather3);

        assertEquals(weather1.hashCode(), weather2.hashCode());
    }

    @Test
    void testWindEquality() {
        WeatherResponse.Wind wind1 = new WeatherResponse.Wind(5.5);
        WeatherResponse.Wind wind2 = new WeatherResponse.Wind(5.5);
        WeatherResponse.Wind wind3 = new WeatherResponse.Wind(7.0);

        assertEquals(wind1, wind2);
        assertNotEquals(wind1, wind3);

        assertEquals(wind1.hashCode(), wind2.hashCode());
    }

    @Test
    void testSysEquality() {
        WeatherResponse.Sys sys1 = new WeatherResponse.Sys(1234567890L, 1234567900L);
        WeatherResponse.Sys sys2 = new WeatherResponse.Sys(1234567890L, 1234567900L);
        WeatherResponse.Sys sys3 = new WeatherResponse.Sys(1234567800L, 1234567800L);

        assertEquals(sys1, sys2);
        assertNotEquals(sys1, sys3);

        assertEquals(sys1.hashCode(), sys2.hashCode());
    }

    @Test
    void testCompleteWeatherResponseEquality() {
        WeatherResponse response1 = createCompleteResponse("London", 290.15);
        WeatherResponse response2 = createCompleteResponse("London", 290.15);
        WeatherResponse response3 = createCompleteResponse("Paris", 285.0);

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);

        assertEquals(response1.hashCode(), response2.hashCode());
    }

    /**
     * Helper method to create complete WeatherResponse
     */
    private WeatherResponse createCompleteResponse(String city, double temp) {
        WeatherResponse response = new WeatherResponse();
        response.setName(city);

        WeatherResponse.Temperature temperature = new WeatherResponse.Temperature(temp, temp - 2);
        response.setTemperature(temperature);

        WeatherResponse.Weather weather = new WeatherResponse.Weather("Clear", "clear sky");
        response.setWeather(weather);

        WeatherResponse.Wind wind = new WeatherResponse.Wind(5.5);
        response.setWind(wind);

        response.setVisibility(10000);
        response.setDatetime(1234567890L);

        WeatherResponse.Sys sys = new WeatherResponse.Sys(1234567800L, 1234567900L);
        response.setSys(sys);

        response.setTimezone(3600);

        return response;
    }
}