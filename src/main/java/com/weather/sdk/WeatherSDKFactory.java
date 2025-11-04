package com.weather.sdk;

import com.weather.sdk.exception.WeatherSDKException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Фабрика для создания и управления экземплярами WeatherSDK
 * 
 * Гарантирует, что для каждого уникального API ключа существует только один экземпляр SDK.
 * Это предотвращает дублирование polling потоков и оптимизирует использование ресурсов.
 * 
 * Пример использования:
 * <pre>
 * WeatherSDK sdk1 = WeatherSDKFactory.getInstance("key1", OperationMode.POLLING);
 * WeatherSDK sdk2 = WeatherSDKFactory.getInstance("key1", OperationMode.POLLING); // вернёт sdk1
 * 
 * WeatherSDKFactory.removeInstance("key1"); // удаляет и закрывает SDK
 * </pre>
 */
public class WeatherSDKFactory {
    
    private static final Logger LOGGER = Logger.getLogger(WeatherSDKFactory.class.getName());
    
    // Потокобезопасная карта для хранения инстансов
    private static final Map<String, WeatherSDK> instances = new ConcurrentHashMap<>();
    
    // Приватный конструктор для предотвращения создания экземпляров
    private WeatherSDKFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Получает или создаёт экземпляр WeatherSDK для указанного API ключа
     * 
     * @param apiKey API ключ OpenWeather
     * @param mode режим работы SDK
     * @return экземпляр WeatherSDK
     * @throws WeatherSDKException если не удалось создать SDK
     */
    public static synchronized WeatherSDK getInstance(String apiKey, WeatherSDK.OperationMode mode) 
            throws WeatherSDKException {
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherSDKException("API key cannot be null or empty");
        }
        
        String key = apiKey.trim();
        
        // Если инстанс уже существует, возвращаем его
        if (instances.containsKey(key)) {
            WeatherSDK existingInstance = instances.get(key);
            LOGGER.log(Level.INFO, "Returning existing WeatherSDK instance for key: {0}", 
                      maskApiKey(key));
            return existingInstance;
        }
        
        // Создаём новый инстанс
        WeatherSDK newInstance = new WeatherSDK(key, mode);
        instances.put(key, newInstance);
        
        LOGGER.log(Level.INFO, "Created new WeatherSDK instance for key: {0}", 
                  maskApiKey(key));
        
        return newInstance;
    }
    
    /**
     * Удаляет экземпляр SDK для указанного API ключа
     * 
     * @param apiKey API ключ
     * @return true если экземпляр был удалён, false если его не существовало
     */
    public static synchronized boolean removeInstance(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        String key = apiKey.trim();
        WeatherSDK instance = instances.remove(key);
        
        if (instance != null) {
            instance.close();
            LOGGER.log(Level.INFO, "Removed WeatherSDK instance for key: {0}", 
                      maskApiKey(key));
            return true;
        }
        
        return false;
    }
    
    /**
     * Удаляет все экземпляры SDK
     */
    public static synchronized void removeAllInstances() {
        instances.forEach((key, sdk) -> {
            sdk.close();
            LOGGER.log(Level.INFO, "Closed WeatherSDK instance for key: {0}", 
                      maskApiKey(key));
        });
        instances.clear();
        LOGGER.log(Level.INFO, "All WeatherSDK instances removed");
    }
    
    /**
     * Проверяет, существует ли экземпляр для указанного API ключа
     */
    public static boolean hasInstance(String apiKey) {
        if (apiKey == null) {
            return false;
        }
        return instances.containsKey(apiKey.trim());
    }
    
    /**
     * Возвращает количество активных экземпляров SDK
     */
    public static int getInstanceCount() {
        return instances.size();
    }
    
    /**
     * Маскирует API ключ для безопасного логирования
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
