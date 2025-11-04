package examples;

import com.weather.sdk.WeatherSDK;
import com.weather.sdk.WeatherSDKFactory;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherData;

/**
 * Примеры использования WeatherSDK.
 * 
 * ⚠️ Замени "YOUR_API_KEY" на свой реальный API ключ из https://openweathermap.org
 */
public class WeatherSDKExample {
    
    private static final String API_KEY = "YOUR_API_KEY"; // 👈 Вставь свой API ключ здесь!
    
    public static void main(String[] args) {
        System.out.println("=== Примеры использования Weather SDK ===\n");
        
        // Пример 1: Простое использование в режиме ON_DEMAND
        example1_BasicUsage();
        
        // Пример 2: Использование в режиме POLLING
        example2_PollingMode();
        
        // Пример 3: Демонстрация кэширования
        example3_CachingDemo();
        
        // Пример 4: Обработка ошибок
        example4_ErrorHandling();
        
        // Пример 5: Работа с несколькими экземплярами SDK
        example5_MultipleInstances();
        
        // Пример 6: Try-with-resources (автоматическое закрытие)
        example6_TryWithResources();
    }
    
    /**
     * Пример 1: Базовое использование в режиме ON_DEMAND.
     */
    private static void example1_BasicUsage() {
        System.out.println("📍 Пример 1: Базовое использование\n");
        
        try {
            // Создаем экземпляр SDK в режиме ON_DEMAND
            WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.ON_DEMAND);
            
            // Получаем погоду для Москвы
            WeatherData weather = sdk.getWeather("Moscow");
            
            // Выводим информацию
            System.out.println("Город: " + weather.getName());
            System.out.println("Погода: " + weather.getWeather().getMain() + 
                             " (" + weather.getWeather().getDescription() + ")");
            System.out.println("Температура: " + 
                String.format("%.1f°C", weather.getTemperature().getTempCelsius()));
            System.out.println("Ощущается как: " + 
                String.format("%.1f°C", weather.getTemperature().getFeelsLikeCelsius()));
            System.out.println("Ветер: " + weather.getWind().getSpeed() + " м/с");
            System.out.println("Видимость: " + weather.getVisibility() + " м\n");
            
            // Удаляем экземпляр
            WeatherSDKFactory.removeInstance(API_KEY);
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Пример 2: Использование в режиме POLLING.
     * В этом режиме данные обновляются автоматически каждые 5 минут.
     */
    private static void example2_PollingMode() {
        System.out.println("📍 Пример 2: Режим POLLING\n");
        
        try {
            // Создаем SDK в режиме POLLING
            WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.POLLING);
            
            // Первый запрос - данные загружаются с API
            System.out.println("Первый запрос (загрузка с API):");
            WeatherData weather1 = sdk.getWeather("London");
            printWeatherShort(weather1);
            
            // Добавляем еще города
            sdk.getWeather("Paris");
            sdk.getWeather("Berlin");
            
            System.out.println("В кэше городов: " + sdk.getCacheSize());
            System.out.println("Polling будет автоматически обновлять данные каждые 5 минут\n");
            
            // Последующие запросы вернут данные из кэша мгновенно
            System.out.println("Повторный запрос (из кэша, мгновенно):");
            WeatherData weather2 = sdk.getWeather("London");
            printWeatherShort(weather2);
            
            WeatherSDKFactory.removeInstance(API_KEY);
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Пример 3: Демонстрация работы кэша.
     */
    private static void example3_CachingDemo() {
        System.out.println("📍 Пример 3: Кэширование (ON_DEMAND режим)\n");
        
        try {
            WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.ON_DEMAND);
            
            // Первый запрос - идет к API
            System.out.println("Первый запрос Tokyo (с API):");
            long start1 = System.currentTimeMillis();
            sdk.getWeather("Tokyo");
            long time1 = System.currentTimeMillis() - start1;
            System.out.println("Время: " + time1 + " мс\n");
            
            // Второй запрос - из кэша (должен быть быстрее)
            System.out.println("Второй запрос Tokyo (из кэша):");
            long start2 = System.currentTimeMillis();
            sdk.getWeather("Tokyo");
            long time2 = System.currentTimeMillis() - start2;
            System.out.println("Время: " + time2 + " мс");
            System.out.println("Кэш сработал! Ускорение в " + (time1 / Math.max(time2, 1)) + " раз\n");
            
            // Демонстрация лимита кэша (10 городов)
            System.out.println("Добавляем 10 городов в кэш:");
            String[] cities = {"New York", "Los Angeles", "Chicago", "Houston", 
                             "Phoenix", "Philadelphia", "San Antonio", "San Diego",
                             "Dallas", "San Jose"};
            
            for (String city : cities) {
                sdk.getWeather(city);
                System.out.println("  Добавлен: " + city + " (в кэше: " + sdk.getCacheSize() + ")");
            }
            
            System.out.println("\nПопытка добавить 11-й город:");
            sdk.getWeather("Austin");
            System.out.println("  Добавлен Austin, самый старый город удален из кэша");
            System.out.println("  В кэше осталось: " + sdk.getCacheSize() + " городов\n");
            
            WeatherSDKFactory.removeInstance(API_KEY);
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Пример 4: Обработка различных типов ошибок.
     */
    private static void example4_ErrorHandling() {
        System.out.println("📍 Пример 4: Обработка ошибок\n");
        
        try {
            WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.ON_DEMAND);
            
            // Попытка получить погоду для несуществующего города
            System.out.println("Запрос несуществующего города:");
            try {
                sdk.getWeather("NonExistentCityXYZ123");
            } catch (WeatherSDKException e) {
                System.out.println("  ❌ Ожидаемая ошибка: " + e.getMessage());
            }
            
            // Попытка передать пустое название
            System.out.println("\nЗапрос с пустым названием:");
            try {
                sdk.getWeather("");
            } catch (WeatherSDKException e) {
                System.out.println("  ❌ Ожидаемая ошибка: " + e.getMessage());
            }
            
            System.out.println();
            WeatherSDKFactory.removeInstance(API_KEY);
            
        } catch (WeatherSDKException e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Пример 5: Работа с несколькими API ключами.
     */
    private static void example5_MultipleInstances() {
        System.out.println("📍 Пример 5: Множественные экземпляры SDK\n");
        
        String apiKey1 = API_KEY;
        String apiKey2 = "ANOTHER_API_KEY"; // Для демонстрации
        
        try {
            // Создаем первый экземпляр
            WeatherSDK sdk1 = WeatherSDKFactory.getInstance(apiKey1, SDKMode.ON_DEMAND);
            System.out.println("✅ Создан SDK с первым API ключом");
            
            // Попытка создать второй экземпляр с тем же ключом
            WeatherSDK sdk1_duplicate = WeatherSDKFactory.getInstance(apiKey1, SDKMode.ON_DEMAND);
            System.out.println("✅ Получен существующий SDK (тот же объект): " + 
                             (sdk1 == sdk1_duplicate));
            
            // Попытка создать SDK с тем же ключом, но другим режимом
            System.out.println("\nПопытка создать SDK с тем же ключом, но режимом POLLING:");
            try {
                WeatherSDK sdk1_different_mode = WeatherSDKFactory.getInstance(apiKey1, SDKMode.POLLING);
            } catch (WeatherSDKException e) {
                System.out.println("  ❌ Ожидаемая ошибка: " + e.getMessage());
            }
            
            System.out.println("\nВсего активных экземпляров: " + WeatherSDKFactory.getInstanceCount());
            
            // Удаляем все экземпляры
            WeatherSDKFactory.removeAllInstances();
            System.out.println("После удаления всех экземпляров: " + WeatherSDKFactory.getInstanceCount() + "\n");
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Пример 6: Try-with-resources для автоматического освобождения ресурсов.
     */
    private static void example6_TryWithResources() {
        System.out.println("📍 Пример 6: Try-with-resources (рекомендуемый подход)\n");
        
        try (WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY, SDKMode.POLLING)) {
            
            System.out.println("SDK создан и будет автоматически закрыт");
            
            WeatherData weather = sdk.getWeather("Sydney");
            printWeatherShort(weather);
            
            System.out.println("При выходе из блока try SDK автоматически закроется\n");
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
        
        // SDK уже закрыт, очищаем из фабрики
        WeatherSDKFactory.removeInstance(API_KEY);
    }
    
    /**
     * Вспомогательный метод для краткого вывода погоды.
     */
    private static void printWeatherShort(WeatherData weather) {
        System.out.println("  " + weather.getName() + ": " + 
                         weather.getWeather().getMain() + ", " +
                         String.format("%.1f°C", weather.getTemperature().getTempCelsius()));
    }
}
