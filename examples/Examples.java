package com.weather.sdk.examples;

import com.weather.sdk.WeatherSDK;
import com.weather.sdk.WeatherSDKFactory;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherResponse;

/**
 * Примеры использования Weather SDK
 */
public class Examples {
    
    private static final String API_KEY = "your-api-key-here";
    
    public static void main(String[] args) {
        // Пример 1: Базовое использование
        basicUsageExample();
        
        // Пример 2: Использование с фабрикой
        factoryExample();
        
        // Пример 3: Polling режим
        pollingModeExample();
        
        // Пример 4: Обработка ошибок
        errorHandlingExample();
    }
    
    /**
     * Пример 1: Базовое использование SDK в режиме ON_DEMAND
     */
    public static void basicUsageExample() {
        System.out.println("=== Пример 1: Базовое использование ===\n");
        
        try (WeatherSDK sdk = new WeatherSDK(API_KEY, WeatherSDK.OperationMode.ON_DEMAND)) {
            
            // Запрашиваем погоду для города
            WeatherResponse weather = sdk.getWeather("London");
            
            // Выводим информацию
            printWeatherInfo(weather);
            
            // Повторный запрос - данные будут взяты из кэша
            System.out.println("\nПовторный запрос (из кэша):");
            WeatherResponse cachedWeather = sdk.getWeather("London");
            printWeatherInfo(cachedWeather);
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Пример 2: Использование фабрики для управления экземплярами
     */
    public static void factoryExample() {
        System.out.println("\n=== Пример 2: Использование фабрики ===\n");
        
        try {
            // Создаём первый экземпляр
            WeatherSDK sdk1 = WeatherSDKFactory.getInstance(API_KEY, WeatherSDK.OperationMode.ON_DEMAND);
            System.out.println("Создан SDK1");
            
            // Попытка создать второй экземпляр с тем же ключом
            WeatherSDK sdk2 = WeatherSDKFactory.getInstance(API_KEY, WeatherSDK.OperationMode.ON_DEMAND);
            System.out.println("SDK2 = SDK1: " + (sdk1 == sdk2)); // true
            
            // Получаем погоду
            WeatherResponse weather = sdk1.getWeather("Paris");
            printWeatherInfo(weather);
            
            // Удаляем экземпляр
            WeatherSDKFactory.removeInstance(API_KEY);
            System.out.println("SDK удалён из фабрики");
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Пример 3: Использование режима POLLING
     */
    public static void pollingModeExample() {
        System.out.println("\n=== Пример 3: Polling режим ===\n");
        
        try (WeatherSDK sdk = new WeatherSDK(API_KEY, WeatherSDK.OperationMode.POLLING)) {
            
            // Запрашиваем погоду для нескольких городов
            String[] cities = {"Tokyo", "New York", "Moscow"};
            
            for (String city : cities) {
                WeatherResponse weather = sdk.getWeather(city);
                System.out.println(city + ": " + weather.getTemperature().getTemp() + "K");
            }
            
            System.out.println("\nГородов в кэше: " + sdk.getCachedCitiesCount());
            System.out.println("В режиме POLLING данные обновляются автоматически каждые 10 минут");
            
            // В реальном приложении SDK продолжит работать
            // и обновлять данные в фоне
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Пример 4: Обработка различных ошибок
     */
    public static void errorHandlingExample() {
        System.out.println("\n=== Пример 4: Обработка ошибок ===\n");
        
        // Ошибка: неверный API ключ
        try {
            WeatherSDK sdk = new WeatherSDK("invalid-key", WeatherSDK.OperationMode.ON_DEMAND);
            sdk.getWeather("London");
        } catch (WeatherSDKException e) {
            System.out.println("Ожидаемая ошибка (неверный ключ): " + e.getMessage());
        }
        
        // Ошибка: несуществующий город
        try (WeatherSDK sdk = new WeatherSDK(API_KEY, WeatherSDK.OperationMode.ON_DEMAND)) {
            sdk.getWeather("NonExistentCity12345");
        } catch (WeatherSDKException e) {
            System.out.println("Ожидаемая ошибка (город не найден): " + e.getMessage());
        }
        
        // Ошибка: пустое название города
        try (WeatherSDK sdk = new WeatherSDK(API_KEY, WeatherSDK.OperationMode.ON_DEMAND)) {
            sdk.getWeather("");
        } catch (WeatherSDKException e) {
            System.out.println("Ожидаемая ошибка (пустое имя): " + e.getMessage());
        }
    }
    
    /**
     * Вспомогательный метод для красивого вывода информации о погоде
     */
    private static void printWeatherInfo(WeatherResponse weather) {
        System.out.println("Город: " + weather.getName());
        System.out.println("Погода: " + weather.getWeather().getMain() + 
                          " (" + weather.getWeather().getDescription() + ")");
        System.out.println("Температура: " + weather.getTemperature().getTemp() + "K");
        System.out.println("Ощущается как: " + weather.getTemperature().getFeelsLike() + "K");
        System.out.println("Скорость ветра: " + weather.getWind().getSpeed() + " м/с");
        System.out.println("Видимость: " + weather.getVisibility() + " м");
    }
}
