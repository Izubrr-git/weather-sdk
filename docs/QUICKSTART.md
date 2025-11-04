# Быстрый старт с Weather SDK ⚡

Это краткое руководство поможет тебе начать работу с Weather SDK за 5 минут!

## Шаг 1: Получи API ключ 🔑

1. Зарегистрируйся на [OpenWeatherMap](https://openweathermap.org/appid)
2. Перейди в раздел **API keys**
3. Скопируй свой API ключ
4. ⏱️ Подожди 10-15 минут пока ключ активируется

## Шаг 2: Добавь зависимость 📦

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

### Или используй JAR напрямую

```bash
# Скачай JAR файл и добавь в classpath
java -cp weather-sdk-1.0.0.jar:. YourApp
```

## Шаг 3: Напиши свой первый код 🚀

### Простейший пример

```java
import com.weather.sdk.*;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.model.WeatherData;

public class QuickStart {
    public static void main(String[] args) {
        String apiKey = "твой_api_ключ_здесь";
        
        try {
            // 1. Создай SDK
            WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);
            
            // 2. Получи погоду
            WeatherData weather = sdk.getWeather("Moscow");
            
            // 3. Используй данные
            System.out.println("🌍 Город: " + weather.getName());
            System.out.println("🌡️  Температура: " + 
                String.format("%.1f°C", weather.getTemperature().getTempCelsius()));
            System.out.println("☁️  Погода: " + weather.getWeather().getDescription());
            
            // 4. Закрой SDK
            WeatherSDKFactory.removeInstance(apiKey);
            
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
```

### Запусти!

```bash
javac QuickStart.java
java QuickStart
```

**Ожидаемый результат:**
```
🌍 Город: Moscow
🌡️  Температура: -5.2°C
☁️  Погода: scattered clouds
```

## Шаг 4: Изучи основные возможности 💡

### Работа с несколькими городами

```java
WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);

String[] cities = {"Moscow", "London", "Paris", "Tokyo"};
for (String city : cities) {
    try {
        WeatherData weather = sdk.getWeather(city);
        System.out.printf("%s: %.1f°C%n", 
            weather.getName(), 
            weather.getTemperature().getTempCelsius()
        );
    } catch (Exception e) {
        System.err.println(city + ": Ошибка - " + e.getMessage());
    }
}

WeatherSDKFactory.removeInstance(apiKey);
```

### Режим POLLING (автообновление)

```java
// Создай SDK в режиме POLLING
WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.POLLING);

// Первый запрос загружает данные
sdk.getWeather("London");
sdk.getWeather("Paris");

// Данные будут автоматически обновляться каждые 5 минут!
// Все последующие запросы мгновенные (из кэша)

Thread.sleep(60000); // подожди минуту

// Мгновенный ответ из кэша
WeatherData weather = sdk.getWeather("London");

WeatherSDKFactory.removeInstance(apiKey);
```

### Try-with-resources (рекомендуется)

```java
try (WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND)) {
    WeatherData weather = sdk.getWeather("Berlin");
    System.out.println(weather.getName() + ": " + 
        weather.getTemperature().getTempCelsius() + "°C");
} // SDK автоматически закроется

WeatherSDKFactory.removeInstance(apiKey);
```

## Шаг 5: Обработай ошибки правильно 🛡️

```java
try {
    WeatherSDK sdk = WeatherSDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);
    WeatherData weather = sdk.getWeather("Moscow");
    
    // Твой код здесь
    
} catch (CityNotFoundException e) {
    System.err.println("❌ Город не найден!");
} catch (NetworkException e) {
    System.err.println("❌ Проблема с интернетом!");
} catch (ApiKeyException e) {
    System.err.println("❌ Проблема с API ключом!");
} catch (WeatherSDKException e) {
    System.err.println("❌ Ошибка: " + e.getMessage());
}
```

## 🎯 Что дальше?

### Изучи документацию
- 📖 [README.md](../README.md) - Полная документация
- 🏗️ [ARCHITECTURE.md](ARCHITECTURE.md) - Архитектура SDK
- 💻 [Примеры](../examples/) - Больше примеров кода

### Основные концепции

1. **Два режима работы:**
   - `ON_DEMAND` - запросы по требованию
   - `POLLING` - автоматическое обновление

2. **Автоматическое кэширование:**
   - Данные актуальны 10 минут
   - Максимум 10 городов в кэше

3. **Factory Pattern:**
   - Один SDK на один API ключ
   - Используй `WeatherSDKFactory` для создания

4. **Обработка ошибок:**
   - Специфичные исключения для разных ошибок
   - Всегда обрабатывай исключения

### Полезные советы 💡

✅ **Делай:**
- Используй try-with-resources
- Обрабатывай исключения специфично
- Закрывай SDK после использования
- Выбирай правильный режим для задачи

❌ **Не делай:**
- Не создавай много экземпляров для одного ключа
- Не игнорируй исключения
- Не забывай удалять экземпляры из фабрики
- Не делай слишком частые запросы (лимит API)

## 🆘 Проблемы?

### API ключ не работает
```
Ошибка 401: Invalid API key
```
**Решение:** Подожди 10-15 минут после регистрации для активации ключа.

### Город не найден
```
CityNotFoundException: Город 'Moscw' не найден
```
**Решение:** Проверь правильность написания. Используй английские названия.

### Сетевая ошибка
```
NetworkException: Connection timeout
```
**Решение:** Проверь интернет-соединение и доступность api.openweathermap.org.

### Нужна помощь?
- 📧 Создай Issue на GitHub
- 📚 Изучи [полную документацию](../README.md)
- 💬 Задай вопрос в Discussions

---

Поздравляю! 🎉 Ты готов использовать Weather SDK в своих проектах!
