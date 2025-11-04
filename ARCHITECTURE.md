# Архитектура Weather SDK

## Обзор компонентов

```
┌─────────────────────────────────────────────────────┐
│                    Пользователь                     │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────▼────────────┐
         │  WeatherSDKFactory     │  ← Singleton для управления экземплярами
         │  (Optional)            │
         └───────────┬────────────┘
                     │
         ┌───────────▼────────────┐
         │     WeatherSDK         │  ← Главный класс
         │  - ON_DEMAND           │
         │  - POLLING             │
         └───────┬────────┬───────┘
                 │        │
     ┌───────────▼──┐  ┌──▼──────────────┐
     │ WeatherCache │  │ OpenWeatherClient│
     │ (LRU, 10max) │  │ (HTTP)           │
     └──────────────┘  └──┬───────────────┘
                          │
              ┌───────────▼───────────┐
              │  OpenWeather API      │
              │  (External Service)   │
              └───────────────────────┘
```

## Компоненты

### 1. WeatherSDK (Core)

**Назначение**: Главный класс SDK, предоставляющий API для работы с погодой.

**Ответственности**:
- Управление жизненным циклом SDK
- Координация между кэшем и HTTP клиентом
- Реализация двух режимов работы (ON_DEMAND, POLLING)
- Управление фоновым обновлением (в POLLING режиме)

**Ключевые методы**:
```java
WeatherResponse getWeather(String cityName)
void clearCache()
void close()
```

**Паттерны**:
- AutoCloseable для управления ресурсами
- Strategy (два режима работы)

### 2. WeatherSDKFactory

**Назначение**: Управление созданием и жизненным циклом экземпляров SDK.

**Ответственности**:
- Гарантия единственного экземпляра на API ключ (Singleton per key)
- Предотвращение утечек памяти
- Централизованное управление ресурсами

**Ключевые методы**:
```java
static WeatherSDK getInstance(String apiKey, OperationMode mode)
static boolean removeInstance(String apiKey)
```

**Паттерны**:
- Factory
- Registry Pattern
- Thread-safe с ConcurrentHashMap

### 3. WeatherCache

**Назначение**: Кэширование данных о погоде с автоматической инвалидацией.

**Характеристики**:
- **Стратегия**: LRU (Least Recently Used)
- **Размер**: 10 городов максимум
- **TTL**: 10 минут
- **Thread-safe**: synchronized методы

**Имплементация**:
```java
LinkedHashMap с accessOrder=true + переопределённый removeEldestEntry()
```

### 4. OpenWeatherClient

**Назначение**: HTTP клиент для взаимодействия с OpenWeather API.

**Характеристики**:
- Java 11 HttpClient
- Timeout: 10 секунд
- Детальная обработка ошибок
- URL encoding для названий городов

**Обработка ошибок**:
- 401: Invalid API key
- 404: City not found
- 429: Rate limit exceeded
- 5xx: Server errors

### 5. Model (WeatherResponse, WeatherData)

**WeatherResponse**: DTO для данных погоды (соответствует формату задания).

**WeatherData**: Wrapper с timestamp для валидации актуальности кэша.

## Потоки данных

### On-Demand Mode

```
Пользователь
    │
    │ getWeather("London")
    ▼
WeatherSDK
    │
    │ 1. Проверить кэш
    ▼
WeatherCache
    │
    ├─► [Кэш актуален] ──► Вернуть из кэша
    │
    └─► [Кэш устарел/отсутствует]
         │
         ▼
    OpenWeatherClient
         │
         │ HTTP GET
         ▼
    OpenWeather API
         │
         │ JSON Response
         ▼
    WeatherSDK
         │
         │ Сохранить в кэш
         ▼
    Вернуть пользователю
```

### Polling Mode

```
Инициализация SDK
    │
    │ Запуск ScheduledExecutorService
    ▼
Фоновый поток
    │
    │ Каждые 10 минут
    │
    ▼
Для каждого города в кэше:
    │
    │ HTTP GET
    ▼
OpenWeather API
    │
    │ Обновить кэш
    ▼
Готово

Параллельно:
Пользователь ──► getWeather() ──► Мгновенный ответ из кэша
```

## Потокобезопасность

### Синхронизация

1. **WeatherCache**: все методы synchronized
2. **WeatherSDKFactory**: getInstance() и remove методы synchronized
3. **ScheduledExecutorService**: daemon thread для polling

### Стратегия закрытия

```java
close() → shutdown scheduler → await termination → force shutdown if needed
```

## Обработка ошибок

### Иерархия исключений

```
Exception
    │
    └─► WeatherSDKException
            │
            ├─► Invalid API key
            ├─► City not found
            ├─► Rate limit exceeded
            ├─► Network error
            └─► Server error
```

### Стратегия

- Все публичные методы выбрасывают `WeatherSDKException`
- Детальные сообщения об ошибках
- Логирование через java.util.logging

## Производительность

### Оптимизации

1. **Кэширование**: снижение API запросов на 90%+
2. **LRU стратегия**: O(1) для get/put операций
3. **Polling mode**: zero-latency для пользовательских запросов
4. **Connection pooling**: встроенный в HttpClient

### Метрики

- **Cache hit rate**: ожидается >80% для активных городов
- **Response time**: 
  - Кэш: <1ms
  - API: 100-500ms (зависит от сети)
- **Memory footprint**: ~10KB на город в кэше

## Масштабируемость

### Текущие ограничения

- 10 городов в кэше (требование задания)
- Один поток для polling

### Возможности расширения

1. **Настраиваемый размер кэша**
```java
WeatherSDK(String apiKey, OperationMode mode, int cacheSize)
```

2. **Множественные источники данных**
```java
interface WeatherProvider {
    WeatherResponse getWeather(String city);
}
```

3. **Настраиваемые интервалы polling**
```java
PollingConfig config = new PollingConfig()
    .interval(5, TimeUnit.MINUTES)
    .maxConcurrentUpdates(3);
```

4. **Персистентный кэш**
```java
WeatherCache extends PersistentCache<String, WeatherData>
```

## Безопасность

### Меры защиты

1. **API Key скрыт в логах**: маскировка в WeatherSDKFactory
2. **Валидация входных данных**: null-checks, empty-checks
3. **Timeout защита**: 10 секунд на запрос
4. **Rate limiting awareness**: обработка 429 ошибок

### Рекомендации

- Хранить API ключи в переменных окружения
- Использовать HTTPS (по умолчанию)
- Не логировать чувствительные данные

## Тестирование

### Unit тесты

- WeatherSDKTest: базовая функциональность
- WeatherSDKFactoryTest: управление экземплярами
- WeatherCacheTest: кэширование и LRU
- OpenWeatherClientTest: HTTP клиент (с mock)

### Integration тесты

- Реальные запросы к OpenWeather API
- Тестирование polling режима
- Стресс-тестирование кэша

## CI/CD Pipeline (Рекомендации)

```yaml
build:
  - mvn clean compile
  
test:
  - mvn test
  - mvn jacoco:report (coverage > 80%)
  
quality:
  - mvn checkstyle:check
  - mvn pmd:check
  - SonarQube analysis
  
package:
  - mvn package
  - Generate javadoc
  
deploy:
  - Maven Central
  - GitHub Releases
```

## Зависимости

### Прямые

- **Jackson 2.15.2**: JSON сериализация
- **JUnit 5.10.0**: тестирование (scope: test)

### Неявные

- Java 11 HttpClient (встроен)
- java.util.concurrent (встроен)
- java.util.logging (встроен)

**Преимущества минимальных зависимостей**:
- Малый размер артефакта
- Меньше конфликтов версий
- Быстрая установка
