/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maps {@link Class} objects to values. A value may be retrieved by either the {@link Class} with which it was
 * associated, or a {@link Class} corresponding to a subclass or implementation of this. If there are potentially
 * multiple matches for a given {@link Class} then the 'closest' matching association is returned, starting with the
 * class hierarchy and then any interfaces.
 * <p>
 * Results, both positive and negative, are cached to improve future lookup performance.
 * 
 * @param <T>  the type of the values
 */
public class ClassMap<T> implements Map<Class<?>, T> {

  private final Map<Class<?>, T> _map = new HashMap<>();
  private final Map<Class<?>, T> _cache = new HashMap<>();
  
  private boolean _cacheModified;
  
  @Override
  public synchronized void clear() {
    _map.clear();
    _cache.clear();
  }

  @Override
  public synchronized boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public synchronized boolean containsValue(Object value) {
    return _map.containsValue(value);
  }

  @Override
  public synchronized Set<java.util.Map.Entry<Class<?>, T>> entrySet() {
    // Unmodifiable breaks the interface
    return Collections.unmodifiableSet(_map.entrySet());
  }

  @Override
  public synchronized T get(Object key) {
    Class<?> clazz = (Class<?>) key;
    if (_cache.containsKey(clazz)) {
      // Could be null
      return _cache.get(key);
    }

    if (clazz.getSuperclass() != null) {
      T value = get(clazz.getSuperclass());
      if (value != null) {
        addValueToCache(clazz, value);
        return value;
      }
    }
    
    for (Class<?> intface : clazz.getInterfaces()) {
      T value = get(intface);
      if (value != null) {
        addValueToCache(clazz, value);
        return value;
      }
    }
    
    addValueToCache(clazz, null);
    return null;
  }

  @Override
  public synchronized boolean isEmpty() {
    return _map.isEmpty();
  }

  @Override
  public synchronized Set<Class<?>> keySet() {
    // Unmodifiable breaks the interface
    return Collections.unmodifiableSet(_map.keySet());
  }

  @Override
  public synchronized T put(Class<?> key, T value) {
    T result = _map.put(key, value);
    if (_cacheModified) {
      initCache();
    } else {
      _cache.put(key, value);
    }
    return result;
  }

  @Override
  public synchronized void putAll(Map<? extends Class<?>, ? extends T> m) {
    _map.putAll(m);
    if (_cacheModified) {
      initCache();
    } else {
      _cache.putAll(m);
    }
  }

  @Override
  public synchronized T remove(Object key) {
    T result = _map.remove(key);
    if (_cacheModified) {
      initCache();
    } else {
      _cache.remove(key);
    }
    return result;
  }

  @Override
  public synchronized int size() {
    return _map.size();
  }

  @Override
  public synchronized Collection<T> values() {
    // Unmodifiable breaks the interface
    return Collections.unmodifiableCollection(_map.values());
  }

  //-------------------------------------------------------------------------
  private void addValueToCache(Class<?> key, T value) {
    _cache.put(key, value);
    _cacheModified = true;
  }
  
  private void initCache() {
    _cache.clear();
    _cache.putAll(_map);
    _cacheModified = false;
  }
  
}
