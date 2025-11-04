# Weather SDK

SDK для простой и удобной работы с OpenWeather API на Java.

## 🌟 Основные возможности

- ✅ Простой и понятный API
- 🔄 Два режима работы: On-Demand и Polling
- 💾 Умное кэширование с автоматической инвалидацией (10 минут)
- 🏭 Singleton pattern для управления экземплярами
- ⚡ Асинхронное обновление данных в Polling режиме
- 🛡️ Детальная обработка ошибок
- 📊 Поддержка до 10 городов в кэше одновременно

## 📋 Требования

- Java 11 или выше
- Maven 3.6+
- API ключ OpenWeather ([получить здесь](https://openweathermap.org/api))

## 🚀 Установка

### Maven

```xml
<dependency>
    <groupId>com.weather</groupId>
    <artifactId>weather-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.weather:weather-sdk:1.0.0'
```

### Из исходников

```bash
git clone https://github.com/yourusername/weather-sdk.git
cd weather-sdk
mvn clean install
```

## 📖 Быстрый старт

### Базовый пример

```java
import com.weather.sdk.WeatherSDK;
import com.weather.sdk.model.WeatherResponse;

public class QuickStart {
    public static void main(String[] args) {
        String apiKey = "your-api-key-here";
        
        try (WeatherSDK sdk = new WeatherSDK(apiKey, WeatherSDK.OperationMode.ON_DEMAND)) {
            WeatherResponse weather = sdk.getWeather("London");
            
            System.out.println("Город: " + weather.getName());
            System.out.println("Температура: " + weather.getTemperature().getTemp() + "K");
            System.out.println("Погода: " + weather.getWeather().getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 🎯 Примеры использования

### 1. On-Demand режим

В этом режиме данные запрашиваются только когда вы вызываете `getWeather()`:

```java
WeatherSDK sdk = new WeatherSDK(apiKey, WeatherSDK.OperationMode.ON_DEMAND);

// Первый запрос - идёт к API
WeatherResponse weather1 = sdk.getWeather("Paris");

// Повторный запрос в течение 10 минут - берётся из кэша
WeatherResponse weather2 = sdk.getWeather("Paris");

sdk.close();
```

### 2. Polling режим

SDK автоматически обновляет данные каждые 10 минут:

```java
WeatherSDK sdk = new WeatherSDK(apiKey, WeatherSDK.OperationMode.POLLING);

// Запрашиваем погоду для нескольких городов
sdk.getWeather("Tokyo");
sdk.getWeather("New York");
sdk.getWeather("Moscow");

// Данные будут обновляться автоматически в фоне
// Все последующие запросы будут мгновенными (из кэша)

sdk.close(); // Не забудьте закрыть!
```

### 3. Использование фабрики (рекомендуется)

Фабрика гарантирует, что для каждого API ключа существует только один экземпляр:

```java
// Создаём экземпляр
WeatherSDK sdk1 = WeatherSDKFactory.getInstance(apiKey, OperationMode.POLLING);

// Попытка создать второй - вернёт существующий
WeatherSDK sdk2 = WeatherSDKFactory.getInstance(apiKey, OperationMode.ON_DEMAND);

System.out.println(sdk1 == sdk2); // true

// Используем SDK
WeatherResponse weather = sdk1.getWeather("Berlin");

// Удаляем когда больше не нужен
WeatherSDKFactory.removeInstance(apiKey);
```

### 4. Обработка ошибок

```java
try (WeatherSDK sdk = new WeatherSDK(apiKey, OperationMode.ON_DEMAND)) {
    WeatherResponse weather = sdk.getWeather("London");
    // обработка данных
} catch (WeatherSDKException e) {
    // Обработка специфичных ошибок SDK
    switch (e.getMessage()) {
        case "Invalid API key":
            System.err.println("Проверьте ваш API ключ");
            break;
        case "City not found":
            System.err.println("Город не найден");
            break;
        default:
            System.err.println("Ошибка: " + e.getMessage());
    }
}
```

## 📊 Формат данных

SDK возвращает объект `WeatherResponse` со следующей структурой:

```json
{
  "weather": {
    "main": "Clouds",
    "description": "scattered clouds"
  },
  "temperature": {
    "temp": 269.6,
    "feels_like": 267.57
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.38
  },
  "datetime": 1675744800,
  "sys": {
    "sunrise": 1675751262,
    "sunset": 1675787560
  },
  "timezone": 3600,
  "name": "Zocca"
}
```

### Доступ к данным

```java
WeatherResponse weather = sdk.getWeather("London");

// Погода
String condition = weather.getWeather().getMain(); // "Clouds"
String description = weather.getWeather().getDescription(); // "scattered clouds"

// Температура (в Кельвинах)
double temp = weather.getTemperature().getTemp(); // 269.6
double feelsLike = weather.getTemperature().getFeelsLike(); // 267.57

// Ветер
double windSpeed = weather.getWind().getSpeed(); // 1.38 м/с

// Видимость
int visibility = weather.getVisibility(); // в метрах

// Восход/закат
long sunrise = weather.getSys().getSunrise(); // Unix timestamp
long sunset = weather.getSys().getSunset(); // Unix timestamp

// Прочее
String cityName = weather.getName(); // "Zocca"
int timezone = weather.getTimezone(); // 3600
long datetime = weather.getDatetime(); // Unix timestamp
```

## 🔧 Конфигурация

### Режимы работы

| Режим | Описание | Использование |
|-------|----------|---------------|
| `ON_DEMAND` | Данные обновляются только по запросу | Редкие запросы, экономия ресурсов |
| `POLLING` | Автоматическое обновление каждые 10 минут | Частые запросы, нулевая задержка |

### Кэширование

- **TTL**: 10 минут
- **Размер**: до 10 городов
- **Стратегия**: LRU (Least Recently Used)

При превышении лимита в 10 городов, самый старый город автоматически удаляется из кэша.

## 🛡️ Обработка ошибок

SDK выбрасывает `WeatherSDKException` в следующих случаях:

| Ошибка | Причина |
|--------|---------|
| `Invalid API key` | Неверный или отсутствующий API ключ |
| `City not found` | Город не найден в базе OpenWeather |
| `API rate limit exceeded` | Превышен лимит запросов |
| `Network error` | Проблемы с сетью |
| `OpenWeather API server error` | Ошибка на стороне OpenWeather |

## 📝 Best Practices

### 1. Всегда закрывайте SDK

```java
// Используйте try-with-resources
try (WeatherSDK sdk = new WeatherSDK(apiKey, mode)) {
    // ваш код
}

// Или закрывайте вручную
WeatherSDK sdk = new WeatherSDK(apiKey, mode);
try {
    // ваш код
} finally {
    sdk.close();
}
```

### 2. Используйте фабрику для долгоживущих экземпляров

```java
// Плохо - создаём несколько экземпляров
WeatherSDK sdk1 = new WeatherSDK(apiKey, mode);
WeatherSDK sdk2 = new WeatherSDK(apiKey, mode); // Дубликат!

// Хорошо - переиспользуем экземпляр
WeatherSDK sdk1 = WeatherSDKFactory.getInstance(apiKey, mode);
WeatherSDK sdk2 = WeatherSDKFactory.getInstance(apiKey, mode); // Тот же экземпляр
```

### 3. Выбирайте правильный режим

- **ON_DEMAND**: для редких запросов (< 1 раз в 10 минут)
- **POLLING**: для частых запросов или когда нужна минимальная задержка

### 4. Обрабатывайте ошибки

```java
try {
    WeatherResponse weather = sdk.getWeather(cityName);
} catch (WeatherSDKException e) {
    logger.error("Failed to get weather: {}", e.getMessage());
    // fallback логика
}
```

## 🧪 Тестирование

Запуск тестов:

```bash
mvn test
```

Тесты с покрытием:

```bash
mvn test jacoco:report
```

## 📚 API Reference

### WeatherSDK

#### Конструктор

```java
WeatherSDK(String apiKey, OperationMode mode) throws WeatherSDKException
```

#### Методы

```java
WeatherResponse getWeather(String cityName) throws WeatherSDKException
void clearCache()
OperationMode getMode()
int getCachedCitiesCount()
void close()
```

### WeatherSDKFactory

```java
static WeatherSDK getInstance(String apiKey, OperationMode mode)
static boolean removeInstance(String apiKey)
static void removeAllInstances()
static boolean hasInstance(String apiKey)
static int getInstanceCount()
```

## 🤝 Вклад в проект

Мы приветствуем вклад в проект! Пожалуйста:

1. Форкните репозиторий
2. Создайте ветку для вашей фичи (`git checkout -b feature/AmazingFeature`)
3. Закоммитьте изменения (`git commit -m 'Add some AmazingFeature'`)
4. Запушьте в ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

## 📄 Лицензия

Проект распространяется под лицензией MIT. Подробности в файле `LICENSE`.

## 🔗 Полезные ссылки

- [OpenWeather API Documentation](https://openweathermap.org/api)
- [Javadoc](https://yourdomain.com/weather-sdk/javadoc)
- [GitHub Issues](https://github.com/yourusername/weather-sdk/issues)

## 📧 Поддержка

Если у вас возникли вопросы или проблемы:
- Создайте issue на GitHub
- Напишите на support@example.com
