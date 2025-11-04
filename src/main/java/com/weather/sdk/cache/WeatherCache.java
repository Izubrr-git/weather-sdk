package com.weather.sdk.cache;

import com.weather.sdk.model.WeatherData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * LRU-кэш для хранения данных о погоде
 * 
 * Автоматически удаляет самые старые записи при превышении лимита.
 * Максимальная ёмкость - 10 городов.
 */
public class WeatherCache {
    
    private final int maxSize;
    private final Map<String, WeatherData> cache;
    
    /**
     * Создаёт кэш с заданным размером
     * 
     * @param maxSize максимальное количество городов в кэше
     */
    public WeatherCache(int maxSize) {
        this.maxSize = maxSize;
        // LinkedHashMap с accessOrder=true для LRU
        this.cache = new LinkedHashMap<String, WeatherData>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, WeatherData> eldest) {
                return size() > WeatherCache.this.maxSize;
            }
        };
    }
    
    /**
     * Получает данные о погоде из кэша
     * 
     * @param cityName название города
     * @return данные о погоде или null если не найдены
     */
    public synchronized WeatherData get(String cityName) {
        return cache.get(normalizeCityName(cityName));
    }
    
    /**
     * Сохраняет данные о погоде в кэш
     * 
     * @param cityName название города
     * @param data данные о погоде
     */
    public synchronized void put(String cityName, WeatherData data) {
        cache.put(normalizeCityName(cityName), data);
    }
    
    /**
     * Удаляет данные о погоде из кэша
     * 
     * @param cityName название города
     */
    public synchronized void remove(String cityName) {
        cache.remove(normalizeCityName(cityName));
    }
    
    /**
     * Очищает весь кэш
     */
    public synchronized void clear() {
        cache.clear();
    }
    
    /**
     * Возвращает количество городов в кэше
     */
    public synchronized int size() {
        return cache.size();
    }
    
    /**
     * Возвращает множество названий городов в кэше
     */
    public synchronized Set<String> getCityNames() {
        return Set.copyOf(cache.keySet());
    }
    
    /**
     * Нормализует название города для использования в качестве ключа
     */
    private String normalizeCityName(String cityName) {
        return cityName.trim().toLowerCase();
    }
}
