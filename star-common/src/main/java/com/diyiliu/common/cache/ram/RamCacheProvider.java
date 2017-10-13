package com.diyiliu.common.cache.ram;

import com.diyiliu.common.cache.ICache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description: RamCacheProvider
 * Author: DIYILIU
 * Update: 2016-03-17 16:39
 */
public class RamCacheProvider implements ICache {
    private Map<Object, Object> cacheMap = new ConcurrentHashMap();

    public void clear() {
        cacheMap.clear();
    }

    @Override
    public void put(Object key, Object value) {
        cacheMap.put(key, value);
    }

    @Override
    public void put(Map<?, ?> map) {
        cacheMap.putAll(map);
    }

    @Override
    public boolean containsKey(Object key) {
        return cacheMap.containsKey(key);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object get(Object key) {
        return containsKey(key) ? this.cacheMap.get(key) : null;
    }

    @Override
    public Map<String, Object> get(Collection<Object> keys) {
        if (keys == null || keys.isEmpty())
            return null;
        Map<String, Object> rtnItems = new HashMap<String, Object>();
        Iterator<Object> keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next().toString();
            Object value = this.get(key);
            if (value != null) {
                rtnItems.put(key, value);
            }
        }
        return rtnItems;
    }

    @Override
    public List<Object> getAll(Collection<Object> keys) {
        if (keys == null || keys.isEmpty())
            return null;
        List<Object> values = new ArrayList<Object>();
        Iterator<Object> keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next().toString();
            Object value = this.get(key);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    @Override
    public boolean remove(Object key) {
        this.cacheMap.remove(key);
        return true;
    }

    @Override
    public int size() {
        return this.cacheMap.size();
    }

    @Override
    public Set<Object> getKeys() {
        return this.cacheMap.keySet();
    }
}
