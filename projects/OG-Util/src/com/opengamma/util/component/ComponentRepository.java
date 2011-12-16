/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.Lifecycle;

import com.opengamma.util.ArgumentChecker;

/**
 * A repository for OpenGamma components.
 * <p>
 * The OpenGamma logical architecture consists of a set of components.
 * This repository manages the components.
 * <p>
 * This class uses concurrent collections, but instances are intended to be created
 * from a single thread at startup.
 */
public class ComponentRepository implements Lifecycle {

  /**
   * The thread-local instance.
   */
  private static final ThreadLocal<ComponentRepository> s_threadRepo = new InheritableThreadLocal<ComponentRepository>();

  /**
   * The map of info by type.
   */
  private final ConcurrentMap<Class<?>, ComponentTypeInfo> _infoMap = new ConcurrentHashMap<Class<?>, ComponentTypeInfo>();
  /**
   * The repository of component instances.
   */
  private final ConcurrentMap<ComponentKey, Object> _instanceMap = new ConcurrentHashMap<ComponentKey, Object>();
  /**
   * The repository of RESTful published components.
   */
  private final ConcurrentMap<ComponentKey, Object> _restPublished = new ConcurrentHashMap<ComponentKey, Object>();
  /**
   * The objects with {@code Lifecycle}.
   */
  private final List<Lifecycle> _lifecycles = new ArrayList<Lifecycle>();
  /**
   * The status.
   */
  private volatile Status _status = Status.CREATING;

  /**
   * Creates an instance.
   */
  public ComponentRepository() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the default instance of a component.
   * <p>
   * This finds an instance, choosing the instance registered as the default.
   * This may be used to find both component and infrastructure instances.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @return the component, not null
   * @throws IllegalArgumentException if no component is available
   */
  public <T> T getInstance(Class<T> type) {
    ComponentTypeInfo typeInfo = getTypeInfo(type);
    if (typeInfo.getDefaultClassifier() == null) {
      throw new IllegalArgumentException("No default component available: " + type);
    }
    return getInstance(type, typeInfo.getDefaultClassifier());
  }

  /**
   * Gets an instance of a component.
   * <p>
   * This finds an instance that matches the specified type.
   * This may be used to find both component and infrastructure instances.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @param classifier  the classifier that distinguishes the component, empty for default, not null
   * @return the component instance, not null
   * @throws IllegalArgumentException if no component is available
   */
  public <T> T getInstance(Class<T> type, String classifier) {
    ArgumentChecker.notNull(type, "type");
    ComponentKey key = ComponentKey.of(type, classifier);
    Object result = _instanceMap.get(key);
    if (result == null) {
      throw new IllegalArgumentException("No component available: " + key);
    }
    return type.cast(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type information for the component.
   * <p>
   * This method will not find infrastructure instances.
   * 
   * @param type  the type to get, not null
   * @return the component type information, not null
   * @throws IllegalArgumentException if no component is available
   */
  public ComponentTypeInfo getTypeInfo(Class<?> type) {
    ComponentTypeInfo typeInfo = _infoMap.get(type);
    if (typeInfo == null) {
      throw new IllegalArgumentException("Unknown component: " + type);
    }
    return typeInfo;
  }

  /**
   * Gets the component information.
   * <p>
   * This method will not find infrastructure instances.
   * 
   * @param type  the type to get, not null
   * @param classifier  the classifier that distinguishes the component, empty for default, not null
   * @return the component information, not null
   * @throws IllegalArgumentException if no component is available
   */
  public ComponentInfo getInfo(Class<?> type, String classifier) {
    ArgumentChecker.notNull(type, "type");
    ComponentTypeInfo typeInfo = getTypeInfo(type);
    return typeInfo.getInfo(classifier);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the published components.
   * 
   * @return an unmodifiable copy of the published components, not null
   */
  public Collection<Object> getPublished() {
    return new ArrayList<Object>(_restPublished.values());
  }

  /**
   * Gets the published component map.
   * 
   * @return an unmodifiable copy of the published components, not null
   */
  Map<ComponentKey, Object> getPublishedMap() {
    return Collections.unmodifiableMap(_restPublished);
  }

  //-------------------------------------------------------------------------
  /**
   * Registers the component specifying the info that describes it.
   * <p>
   * If the component implements {@code Lifecycle}, it will be registered.
   * 
   * @param info  the component info to register, not null
   * @param instance  the component instance to register, not null
   * @param makeDefault  true to make it the default
   * @throws IllegalArgumentException if unable to register
   */
  public void registerComponent(ComponentInfo info, Object instance, boolean makeDefault) {
    ArgumentChecker.notNull(info, "info");
    ArgumentChecker.notNull(instance, "instance");
    checkStatus(Status.CREATING);
    
    ComponentKey key = info.toComponentKey();
    try {
      registerInstance(key, instance);
      
      _infoMap.putIfAbsent(info.getType(), new ComponentTypeInfo(info.getType()));
      ComponentTypeInfo typeInfo = getTypeInfo(info.getType());
      typeInfo.getInfoMap().put(info.getClassifier(), info);
      if (makeDefault) {
        typeInfo.setDefaultClassifier(info.getClassifier());
      }
      
    } catch (RuntimeException ex) {
      _status = Status.FAILED;
      throw new RuntimeException("Failed during registration: " + key, ex);
    }
  }

  /**
   * Registers a piece of infrastructure.
   * <p>
   * The infrastructure instance has no component information, but is otherwise equivalent
   * to a component. If the instance implements {@code Lifecycle}, it will be registered.
   * 
   * @param <T>  the type
   * @param type  the type to register under, not null
   * @param classifier  the classifier that distinguishes the component, empty for default, not null
   * @param instance  the component instance to register, not null
   * @throws IllegalArgumentException if unable to register
   */
  public <T> void registerInfrastructure(Class<T> type, String classifier, T instance) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(classifier, "classifier");
    ArgumentChecker.notNull(instance, "instance");
    checkStatus(Status.CREATING);
    
    ComponentKey key = ComponentKey.of(type, classifier);
    try {
      registerInstance(key, instance);
      
    } catch (RuntimeException ex) {
      _status = Status.FAILED;
      throw new RuntimeException("Failed during registration: " + key, ex);
    }
  }

  /**
   * Registers an instance.
   * <p>
   * If the instance implements {@code Lifecycle}, it will be registered.
   * 
   * @param key  the key to register under, not null
   * @param instance  the component instance to register, not null
   * @throws IllegalArgumentException if unable to register
   */
  private void registerInstance(ComponentKey key, Object instance) {
    Object current = _instanceMap.putIfAbsent(key, instance);
    if (current != null) {
      throw new IllegalArgumentException("Component already registered for specified information: " + key);
    }
    if (instance instanceof Lifecycle) {
      registerLifecycle((Lifecycle) instance);
    }
  }

  /**
   * Registers a non-component object implementing {@code Lifecycle}.
   * 
   * @param lifecycleObject  the object that has a lifecycle, not null
   */
  public void registerLifecycle(Lifecycle lifecycleObject) {
    ArgumentChecker.notNull(lifecycleObject, "lifecycleObject");
    checkStatus(Status.CREATING);
    
    try {
      _lifecycles.add(lifecycleObject);
      
    } catch (RuntimeException ex) {
      _status = Status.FAILED;
      throw new RuntimeException("Failed during registering lifecycle: " + lifecycleObject, ex);
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
    checkStatus(Status.CREATING);
    
    ComponentKey key = info.toComponentKey();
    try {
      _restPublished.put(key, resource);
      
    } catch (RuntimeException ex) {
      _status = Status.FAILED;
      throw new RuntimeException("Failed during publishing: " + key, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Marks this repository as complete and ready for use.
   */
  @Override
  public void start() {
    checkStatus(Status.CREATING);
    _status = Status.STARTING;
    try {
      for (Lifecycle obj : _lifecycles) {
        obj.start();
      }
      _status = Status.RUNNING;
      
    } catch (RuntimeException ex) {
      _status = Status.FAILED;
      throw ex;
    }
  }

  /**
   * Stops this repository.
   */
  @Override
  public void stop() {
    checkStatus(Status.RUNNING);
    _status = Status.STOPPING;
    for (Lifecycle obj : _lifecycles) {
      obj.stop();
    }
    _status = Status.STOPPED;
  }

  /**
   * Checks if this repository is running (started).
   * 
   * @return true if running
   */
  @Override
  public boolean isRunning() {
    return _status == Status.RUNNING;
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the thread-loal instance.
   */
  public void pushThreadLocal() {
    s_threadRepo.set(this);
  }

  /**
   * Gets the thread-local instance.
   * 
   * @return the thread-local instance, not null
   */
  public static ComponentRepository getThreadLocal() {
    ComponentRepository repo = s_threadRepo.get();
    if (repo == null) {
      throw new IllegalStateException("ComponentRepository thread-local not set");
    }
    return repo;
  }

  //-------------------------------------------------------------------------
  private void checkStatus(Status status) {
    if (_status != status) {
      throw new IllegalStateException("Invalid repository status, expecetd" + status + " but was " + _status);
    }
  }

  private static enum Status {
    CREATING, STARTING, RUNNING, STOPPING, STOPPED, FAILED,
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(1024);
    buf.append(getClass().getSimpleName()).append(_instanceMap.keySet());
    return buf.toString();
  }

}
