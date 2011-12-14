/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.util.ArgumentChecker;

/**
 * A repository for OpenGamma components.
 * <p>
 * The OpenGamma logical architecture consists of a set of components.
 * This repository manages the components.
 * <p>
 * This class is thread-safe via concurrent collections.
 */
public class ComponentRepository {

  /**
   * The classifier used for the default instance.
   */
  public static final String DEFAULT_CLASSIFIER = "DEFAULT";

  /**
   * The repository of component instances.
   */
  private final ConcurrentMap<ComponentKey, Object> _instanceMap = new ConcurrentHashMap<ComponentKey, Object>();
  /**
   * The repository of component info.
   */
  private final ConcurrentMap<ComponentKey, ComponentInfo> _infoMap = new ConcurrentHashMap<ComponentKey, ComponentInfo>();
  /**
   * The repository of RESTful published components.
   */
  private final ConcurrentMap<ComponentKey, Object> _restPublished = new ConcurrentHashMap<ComponentKey, Object>();
  /**
   * The thread-local instance.
   */
  private static final ThreadLocal<ComponentRepository> s_threadRepo = new InheritableThreadLocal<ComponentRepository>();

  /**
   * Creates an instance.
   */
  public ComponentRepository() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the default instance of a component.
   * <p>
   * This finds a component, choosing the instance registered as the default.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @return the component, not null
   * @throws IllegalArgumentException if no component is available
   */
  public <T> T getInstance(Class<T> type) {
    return getInstance(type, DEFAULT_CLASSIFIER);
  }

  /**
   * Gets an instance of a component.
   * <p>
   * This finds a component that matches the specified type.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @param classifier  the classifier that distinguishes the component, empty for default, not null
   * @return the component instance, not null
   * @throws IllegalArgumentException if no component is available
   */
  public <T> T getInstance(Class<T> type, String classifier) {
    ArgumentChecker.notNull(type, "type");
    ComponentKey key = new ComponentKey(type, classifier);
    Object result = _instanceMap.get(key);
    if (result == null) {
      throw new IllegalArgumentException("No component available: " + key);
    }
    return type.cast(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the component information.
   * 
   * @param type  the type to get, not null
   * @param classifier  the classifier that distinguishes the component, empty for default, not null
   * @return the component information, not null
   * @throws IllegalArgumentException if no component is available
   */
  public ComponentInfo getInfo(Class<?> type, String classifier) {
    ArgumentChecker.notNull(type, "type");
    ComponentKey key = new ComponentKey(type, classifier);
    ComponentInfo result = _infoMap.get(key);
    if (result == null) {
      throw new IllegalArgumentException("No component available: " + key);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Registers the component.
   * 
   * @param info  the component info to register, not null
   * @param instance  the component instance to register, not null
   */
  public void register(ComponentInfo info, Object instance) {
    ArgumentChecker.notNull(info, "info");
    ArgumentChecker.notNull(instance, "instance");
    ComponentKey key = new ComponentKey(info.getType(), info.getClassifier());
    Object current1 = _instanceMap.putIfAbsent(key, instance);
    Object current2 = _infoMap.putIfAbsent(key, info);
    if (current1 != null || current2 != null) {
      throw new IllegalArgumentException("Component already registered for specified information");
    }
  }

  /**
   * Publishes the component as a RESTful API.
   * 
   * @param info  the component info to register, not null
   * @param resource  the RESTful resource, not null
   */
  public void publishRest(ComponentInfo info, Object resource) {
    ArgumentChecker.notNull(info, "info");
    ArgumentChecker.notNull(resource, "resource");
    ComponentKey key = new ComponentKey(info.getType(), info.getClassifier());
    _restPublished.put(key, resource);
  }

  /**
   * Gets the published components.
   * 
   * @return a modifiable copy of the published components, not null
   */
  public List<Object> getPublished() {
    return new ArrayList<Object>(_restPublished.values());
  }

  //-------------------------------------------------------------------------
  /**
   * Sets this instance as the thread-local instance.
   */
  public void pushThreadLocal() {
    s_threadRepo.set(this);
  }

  /**
   * Gets the thread-local instance.
   * 
   * @return the thread-local instance
   */
  public static ComponentRepository getThreadLocal() {
    return s_threadRepo.get();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(1024);
    buf.append(getClass().getSimpleName()).append(_instanceMap.keySet());
    return buf.toString();
  }

  //-------------------------------------------------------------------------
  /**
   * The compound lookup key.
   */
  static final class ComponentKey {
    private final Class<?> _type;
    private final String _classifier;
    ComponentKey(Class<?> type, String classifier) {
      super();
      _type = type;
      _classifier = classifier;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ComponentKey) {
        ComponentKey other = (ComponentKey) obj;
        return _type.equals(other._type) && _classifier.equals(other._classifier);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return _type.hashCode() ^ _classifier.hashCode();
    }

    @Override
    public String toString() {
      return _type + "::" + _classifier;
    }
  }

}
