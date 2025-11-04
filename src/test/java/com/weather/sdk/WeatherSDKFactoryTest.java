package com.weather.sdk;

import com.weather.sdk.exception.WeatherSDKException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit тесты для WeatherSDKFactory
 */
class WeatherSDKFactoryTest {
    
    private static final String API_KEY_1 = "test-key-1";
    private static final String API_KEY_2 = "test-key-2";
    
    @AfterEach
    void cleanup() {
        WeatherSDKFactory.removeAllInstances();
    }
    
    @Test
    void testGetInstanceReturnsNewInstance() throws WeatherSDKException {
        WeatherSDK sdk = WeatherSDKFactory.getInstance(API_KEY_1, WeatherSDK.OperationMode.ON_DEMAND);
        assertNotNull(sdk);
        assertEquals(1, WeatherSDKFactory.getInstanceCount());
    }
    
    @Test
    void testGetInstanceReturnsSameInstanceForSameKey() throws WeatherSDKException {
        WeatherSDK sdk1 = WeatherSDKFactory.getInstance(API_KEY_1, WeatherSDK.OperationMode.ON_DEMAND);
        WeatherSDK sdk2 = WeatherSDKFactory.getInstance(API_KEY_1, WeatherSDK.OperationMode.POLLING);
        
        assertSame(sdk1, sdk2);
        assertEquals(1, WeatherSDKFactory.getInstanceCount());
    }
    
    @Test
    void testGetInstanceCreatesDifferentInstancesForDifferentKeys() throws WeatherSDKException {
        WeatherSDK sdk1 = WeatherSDKFactory.getInstance(API_KEY_1, WeatherSDK.OperationMode.ON_DEMAND);
        WeatherSDK sdk2 = WeatherSDKFactory.getInstance(API_KEY_2, WeatherSDK.OperationMode.ON_DEMAND);
        
        assertNotSame(sdk1, sdk2);
        assertEquals(2, WeatherSDKFactory.getInstanceCount());
    }
    
    @Test
    void testRemoveInstance() throws WeatherSDKException {
        WeatherSDKFactory.getInstance(API_KEY_1, WeatherSDK.OperationMode.ON_DEMAND);
        
        boolean removed = WeatherSDKFactory.removeInstance(API_KEY_1);
        
        assertTrue(removed);
        assertEquals(0, WeatherSDKFactory.getInstanceCount());
    }
    
    @Test
    void testRemoveNonExistentInstance() {
        boolean removed = WeatherSDKFactory.removeInstance("non-existent-key");
        assertFalse(removed);
    }
    
    @Test
    void testHasInstance() throws WeatherSDKException {
        assertFalse(WeatherSDKFactory.hasInstance(API_KEY_1));
        
        WeatherSDKFactory.getInstance(API_KEY_1, WeatherSDK.OperationMode.ON_DEMAND);
        
        assertTrue(WeatherSDKFactory.hasInstance(API_KEY_1));
    }
    
    @Test
    void testRemoveAllInstances() throws WeatherSDKException {
        WeatherSDKFactory.getInstance(API_KEY_1, WeatherSDK.OperationMode.ON_DEMAND);
        WeatherSDKFactory.getInstance(API_KEY_2, WeatherSDK.OperationMode.ON_DEMAND);
        
        WeatherSDKFactory.removeAllInstances();
        
        assertEquals(0, WeatherSDKFactory.getInstanceCount());
    }
    
    @Test
    void testGetInstanceWithNullKey() {
        assertThrows(WeatherSDKException.class, () -> 
            WeatherSDKFactory.getInstance(null, WeatherSDK.OperationMode.ON_DEMAND)
        );
    }
    
    @Test
    void testGetInstanceWithEmptyKey() {
        assertThrows(WeatherSDKException.class, () -> 
            WeatherSDKFactory.getInstance("", WeatherSDK.OperationMode.ON_DEMAND)
        );
    }
}
