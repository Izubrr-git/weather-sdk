# Quick Start Guide

## Получение API ключа

1. Зарегистрируйтесь на https://openweathermap.org/
2. Перейдите в API Keys: https://home.openweathermap.org/api_keys
3. Скопируйте ваш ключ (активация занимает до 2 часов)

## Установка и запуск за 5 минут

### Шаг 1: Клонирование репозитория

```bash
git clone https://github.com/yourusername/weather-sdk.git
cd weather-sdk
```

### Шаг 2: Сборка проекта

```bash
mvn clean install
```

### Шаг 3: Настройка API ключа

Создайте файл `config.properties` в корне проекта:

```properties
openweather.api.key=ваш-api-ключ-здесь
```

Или установите переменную окружения:

```bash
export OPENWEATHER_API_KEY="ваш-api-ключ"
```

### Шаг 4: Запуск примера

```bash
# Компилируем пример
javac -cp "target/weather-sdk-1.0.0.jar:lib/*" examples/SimpleExample.java

# Запускаем
java -cp "target/weather-sdk-1.0.0.jar:lib/*:examples" SimpleExample
```

### Шаг 5: Первый запрос

Создайте файл `MyFirstWeatherApp.java`:

```java
import com.weather.sdk.WeatherSDK;
import com.weather.sdk.model.WeatherResponse;
import com.weather.sdk.exception.WeatherSDKException;

public class MyFirstWeatherApp {
    public static void main(String[] args) {
        // Замените на ваш ключ
        String apiKey = "ваш-api-ключ";
        
        try (WeatherSDK sdk = new WeatherSDK(apiKey, WeatherSDK.OperationMode.ON_DEMAND)) {
            
            // Получаем погоду
            WeatherResponse weather = sdk.getWeather("London");
            
            // Выводим результат
            System.out.println("=== Погода в " + weather.getName() + " ===");
            System.out.println("Температура: " + weather.getTemperature().getTemp() + "K");
            System.out.println("Описание: " + weather.getWeather().getDescription());
            System.out.println("Ветер: " + weather.getWind().getSpeed() + " м/с");
            
        } catch (WeatherSDKException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
```

Запустите:

```bash
javac -cp "target/weather-sdk-1.0.0.jar" MyFirstWeatherApp.java
java -cp "target/weather-sdk-1.0.0.jar:." MyFirstWeatherApp
```

## Использование в Maven проекте

### pom.xml

```xml
<dependency>
    <groupId>com.weather</groupId>
    <artifactId>weather-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Код

```java
WeatherSDK sdk = new WeatherSDK(apiKey, OperationMode.ON_DEMAND);
WeatherResponse weather = sdk.getWeather("Paris");
System.out.println(weather.getName() + ": " + weather.getTemperature().getTemp() + "K");
sdk.close();
```

## Часто задаваемые вопросы

### Q: Как получить температуру в Цельсиях?

```java
double kelvin = weather.getTemperature().getTemp();
double celsius = kelvin - 273.15;
System.out.println(celsius + "°C");
```

### Q: Как проверить работает ли мой API ключ?

```java
try (WeatherSDK sdk = new WeatherSDK(apiKey, OperationMode.ON_DEMAND)) {
    sdk.getWeather("London");
    System.out.println("✓ API ключ работает!");
} catch (WeatherSDKException e) {
    System.out.println("✗ Ошибка: " + e.getMessage());
}
```

### Q: Какой режим выбрать?

- **ON_DEMAND**: если запрашиваете погоду редко (раз в 10+ минут)
- **POLLING**: если нужны частые обновления без задержек

### Q: Сколько городов можно хранить в кэше?

Максимум 10. При добавлении 11-го, самый старый автоматически удаляется.

### Q: Как долго данные актуальны?

10 минут. После этого при следующем запросе данные обновятся автоматически.

## Примеры для разных сценариев

### Веб-приложение

```java
@RestController
public class WeatherController {
    private final WeatherSDK sdk;
    
    public WeatherController() throws WeatherSDKException {
        this.sdk = new WeatherSDK(
            System.getenv("OPENWEATHER_API_KEY"), 
            OperationMode.POLLING
        );
    }
    
    @GetMapping("/weather/{city}")
    public WeatherResponse getWeather(@PathVariable String city) 
            throws WeatherSDKException {
        return sdk.getWeather(city);
    }
}
```

### Консольное приложение

```java
public class WeatherCLI {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java WeatherCLI <city>");
            return;
        }
        
        String apiKey = System.getenv("OPENWEATHER_API_KEY");
        String city = args[0];
        
        try (WeatherSDK sdk = new WeatherSDK(apiKey, OperationMode.ON_DEMAND)) {
            WeatherResponse w = sdk.getWeather(city);
            System.out.printf("%s: %.1f°C, %s%n",
                w.getName(),
                w.getTemperature().getTemp() - 273.15,
                w.getWeather().getDescription()
            );
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Background Service

```java
@Service
public class WeatherUpdateService {
    private final WeatherSDK sdk;
    
    @Autowired
    public WeatherUpdateService() throws WeatherSDKException {
        this.sdk = WeatherSDKFactory.getInstance(
            System.getenv("OPENWEATHER_API_KEY"),
            OperationMode.POLLING
        );
    }
    
    @Scheduled(fixedRate = 300000) // каждые 5 минут
    public void updateWeather() {
        List<String> cities = getCitiesFromDatabase();
        cities.forEach(city -> {
            try {
                WeatherResponse weather = sdk.getWeather(city);
                saveToDatabase(city, weather);
            } catch (Exception e) {
                logger.error("Failed to update {}", city, e);
            }
        });
    }
    
    @PreDestroy
    public void cleanup() {
        sdk.close();
    }
}
```

## Следующие шаги

1. Изучите [README.md](README.md) для полной документации
2. Посмотрите [ARCHITECTURE.md](ARCHITECTURE.md) для понимания внутреннего устройства
3. Прочитайте [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) для production-ready практик
4. Запустите примеры из директории `examples/`
5. Напишите unit тесты для вашего кода

## Поддержка

- GitHub Issues: https://github.com/yourusername/weather-sdk/issues
- Email: support@example.com
- Документация API: https://openweathermap.org/api
