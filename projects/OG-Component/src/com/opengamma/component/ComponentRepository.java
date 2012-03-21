/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.web.context.ServletContextAware;

import com.opengamma.component.rest.RestComponents;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;

/**
 * A repository for OpenGamma components.
 * <p>
 * The OpenGamma logical architecture consists of a set of components.
 * This repository manages the components.
 * <p>
 * This class uses concurrent collections, but instances are intended to be created
 * from a single thread at startup.
 */
public class ComponentRepository implements Lifecycle, ServletContextAware {

  /**
   * The key used to refer to repository in the servlet context.
   */
  public static final String SERVLET_CONTEXT_KEY = ComponentRepository.class.getName();
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ComponentRepository.class);
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
  private final RestComponents _restPublished = new RestComponents();
  /**
   * The objects with {@code Lifecycle}.
   */
  private final List<Lifecycle> _lifecycles = new ArrayList<Lifecycle>();
  /**
   * The objects with {@code ServletContextAware}.
   */
  private final List<ServletContextAware> _servletContextAware = new ArrayList<ServletContextAware>();
  /**
   * The status.
   */
  private AtomicReference<Status> _status = new AtomicReference<Status>(Status.CREATING);

  /**
   * Creates an instance.
   */
  public ComponentRepository() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets all the instances.
   * <p>
   * This method will return all registered instances.
   * 
   * @return the instance map, not null
   */
  public Map<ComponentKey, Object> getInstanceMap() {
    return new HashMap<ComponentKey, Object>(_instanceMap);
  }

  /**
   * Gets all the instances for the specified type.
   * <p>
   * This method will return all registered instances of the specified type.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @return the modifiable instances, not null
   */
  public <T> Collection<T> getInstances(Class<T> type) {
    ArgumentChecker.notNull(type, "type");
    List<T> result = new ArrayList<T>();
    for (ComponentInfo info : getInfos(type)) {
      result.add(type.cast(getInstance(info)));
    }
    return result;
  }

  /**
   * Gets all the component information objects for the specified type.
   * <p>
   * This method will return all registered information objects for the specified type.
   * 
   * @param type  the type to get, not null
   * @return the component informations, not null
   */
  public Collection<ComponentInfo> getInfos(Class<?> type) {
    ArgumentChecker.notNull(type, "type");
    ComponentTypeInfo info = findTypeInfo(type);
    ArrayList<ComponentInfo> result = new ArrayList<ComponentInfo>();
    if (info != null) {
      result.addAll(info.getInfoMap().values());
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets an instance of a component.
   * <p>
   * This finds an instance that matches the specified type.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @param classifier  the classifier that distinguishes the component, not null
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

  /**
   * Gets an instance of a component.
   * <p>
   * This finds an instance that matches the specified information.
   * 
   * @param info  the component info, not null
   * @return the component instance, not null
   * @throws IllegalArgumentException if no component is available
   */
  public Object getInstance(ComponentInfo info) {
    ArgumentChecker.notNull(info, "info");
    ComponentKey key = info.toComponentKey();
    Object result = _instanceMap.get(key);
    if (result == null) {
      throw new IllegalArgumentException("No component available: " + key);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets all the available type information.
   * 
   * @return the component type information, not null
   * @throws IllegalArgumentException if no component is available
   */
  public Collection<ComponentTypeInfo> getTypeInfo() {
    return new ArrayList<ComponentTypeInfo>(_infoMap.values());
  }

  /**
   * Gets type information by type.
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

  //-------------------------------------------------------------------------
  /**
   * Gets component information by type and classifier.
   * 
   * @param type  the type to get, not null
   * @param classifier  the classifier that distinguishes the component, not null
   * @return the component information, not null
   * @throws IllegalArgumentException if no component is available
   */
  public ComponentInfo getInfo(Class<?> type, String classifier) {
    ArgumentChecker.notNull(type, "type");
    ComponentTypeInfo typeInfo = getTypeInfo(type);
    return typeInfo.getInfo(classifier);
  }

  /**
   * Gets component information for an instance.
   * 
   * @param instance  the instance to find info for, not null
   * @return the component information, not null
   * @throws IllegalArgumentException if no component is available
   */
  public ComponentInfo getInfo(Object instance) {
    for (Entry<ComponentKey, Object> entry : _instanceMap.entrySet()) {
      if (entry.getValue() == instance) {
        return getInfo(entry.getKey().getType(), entry.getKey().getClassifier());
      }
    }
    throw new IllegalArgumentException("Unknown component instance: " + instance);
  }

  //-------------------------------------------------------------------------
  /**
   * Finds the type information for the component.
   * <p>
   * This method is lenient, ignoring case and matching simple type names.
   * 
   * @param typeName  the name of the type to get, case insensitive, not null
   * @return the component type information, null if not found
   */
  public ComponentTypeInfo findTypeInfo(String typeName) {
    ArgumentChecker.notNull(typeName, "type");
    for (Class<?> realType : _infoMap.keySet()) {
      if (realType.getName().equalsIgnoreCase(typeName) || realType.getSimpleName().equalsIgnoreCase(typeName)) {
        return getTypeInfo(realType);
      }
    }
    return null;
  }

  /**
   * Finds the type information for the component.
   * <p>
   * This method is lenient, returning null if not found.
   * 
   * @param type  the type to get, not null
   * @return the component information, null if not found
   */
  public ComponentTypeInfo findTypeInfo(Class<?> type) {
    ArgumentChecker.notNull(type, "type");
    return _infoMap.get(type);
  }

  //-------------------------------------------------------------------------
  /**
   * Finds the component information.
   * <p>
   * This method is lenient, ignoring case and matching simple type names.
   * 
   * @param typeName  the simple name of the type to get, case insensitive, not null
   * @param classifier  the classifier that distinguishes the component, case insensitive, not null
   * @return the component information, null if not found
   */
  public ComponentInfo findInfo(String typeName, String classifier) {
    ArgumentChecker.notNull(typeName, "type");
    ComponentTypeInfo typeInfo = findTypeInfo(typeName);
    if (typeInfo != null) {
      for (String realClassifier : typeInfo.getInfoMap().keySet()) {
        if (realClassifier.equalsIgnoreCase(classifier)) {
          return typeInfo.getInfo(realClassifier);
        }
      }
    }
    return null;
  }

  /**
   * Finds the component information.
   * <p>
   * This method is lenient, ignoring case and matching simple type names.
   * 
   * @param type  the type to get, not null
   * @param classifier  the classifier that distinguishes the component, case insensitive, not null
   * @return the component information, null if not found
   */
  public ComponentInfo findInfo(Class<?> type, String classifier) {
    ArgumentChecker.notNull(type, "type");
    ComponentTypeInfo typeInfo = findTypeInfo(type);
    if (typeInfo != null) {
      for (String realClassifier : typeInfo.getInfoMap().keySet()) {
        if (realClassifier.equalsIgnoreCase(classifier)) {
          return typeInfo.getInfo(realClassifier);
        }
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a single instance of the specified component type.
   * <p>
   * This method searches for a single instance of the specified type.
   * If there are no instances or multiple instances, then null is returned.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @return the component information, null if not found
   */
  public <T> T findInstance(Class<T> type) {
    ArgumentChecker.notNull(type, "type");
    ComponentTypeInfo typeInfo = findTypeInfo(type);
    if (typeInfo == null) {
      return null;
    }
    if (typeInfo.getInfoMap().size() != 1) {
      return null;
    }
    Object result = getInstance(typeInfo.getInfoMap().values().iterator().next());
    return type.cast(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the RESTful components.
   * <p>
   * To publish a component over REST, use the methods on this class.
   * 
   * @return the published components, not null
   */
  public RestComponents getRestComponents() {
    return _restPublished;
  }

  //-------------------------------------------------------------------------
  /**
   * Registers the component specifying the info that describes it.
   * <p>
   * If the component implements {@code Lifecycle} or {@code ServletContextAware},
   * it will be registered as though using {@link #registerLifecycle(Lifecycle)} or
   * {@link #registerServletContextAware(ServletContextAware)}.
   * 
   * @param info  the component info to register, not null
   * @param instance  the component instance to register, not null
   * @throws IllegalArgumentException if unable to register
   */
  public void registerComponent(ComponentInfo info, Object instance) {
    ArgumentChecker.notNull(info, "info");
    ArgumentChecker.notNull(instance, "instance");
    checkStatus(Status.CREATING);
    
    ComponentKey key = info.toComponentKey();
    try {
      registerInstance0(key, instance);
      
      _infoMap.putIfAbsent(info.getType(), new ComponentTypeInfo(info.getType()));
      ComponentTypeInfo typeInfo = getTypeInfo(info.getType());
      typeInfo.getInfoMap().put(info.getClassifier(), info);
      registered(info, instance);
      
    } catch (RuntimeException ex) {
      _status.set(Status.FAILED);
      throw new RuntimeException("Failed during registration: " + key, ex);
    }
  }

  /**
   * Registers the component automatically creating the info that describes it.
   * <p>
   * If the component implements {@code Lifecycle} or {@code ServletContextAware},
   * it will be registered as though using {@link #registerLifecycle(Lifecycle)} or
   * {@link #registerServletContextAware(ServletContextAware)}.
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
    ComponentInfo info = new ComponentInfo(type, classifier);
    registerComponent(info, instance);
  }

  /**
   * Registers an instance.
   * <p>
   * If the instance implements {@code Lifecycle} or {@code ServletContextAware}, it will be registered.
   * 
   * @param key  the key to register under, not null
   * @param instance  the component instance to register, not null
   * @throws IllegalArgumentException if unable to register
   */
  private void registerInstance0(ComponentKey key, Object instance) {
    Object current = _instanceMap.putIfAbsent(key, instance);
    if (current != null) {
      throw new IllegalArgumentException("Component already registered for specified information: " + key);
    }
    if (instance instanceof Lifecycle) {
      registerLifecycle0((Lifecycle) instance);
    } else {
      findAndRegisterLifeCycle(instance);
    }
    if (instance instanceof ServletContextAware) {
      registerServletContextAware0((ServletContextAware) instance);
    }
  }

  private void findAndRegisterLifeCycle(final Object obj) {
    if (ReflectionUtils.isCloseable(obj.getClass())) {
      registerLifecycle0(new Lifecycle() {
        @Override
        public void stop() {
          ReflectionUtils.close(obj);
        }
        @Override
        public void start() {
        }
        @Override
        public boolean isRunning() {
          return false;
        }
        @Override
        public String toString() {
          return obj.getClass().getSimpleName() + ":" + obj.toString();
        }
      });
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Registers a non-component object that requires closing or shutdown when
   * the repository stops.
   * 
   * @param obj  the object to close/shutdown, not null
   * @param methodName  the method name to call, not null
   */
  public void registerLifecycleStop(final Object obj, final String methodName) {
    ArgumentChecker.notNull(obj, "object");
    ArgumentChecker.notNull(methodName, "methodName");
    checkStatus(Status.CREATING);
    
    registerLifecycle0(new Lifecycle() {
      @Override
      public void stop() {
        ReflectionUtils.invokeNoArgsNoException(obj, methodName);
      }
      @Override
      public void start() {
      }
      @Override
      public boolean isRunning() {
        return false;
      }
      @Override
      public String toString() {
        return obj.getClass().getSimpleName() + ":" + obj.toString();
      }
    });
    registered(null, obj);
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
      registerLifecycle0(lifecycleObject);
      registered(null, lifecycleObject);
      
    } catch (RuntimeException ex) {
      _status.set(Status.FAILED);
      throw new RuntimeException("Failed during registering Lifecycle: " + lifecycleObject, ex);
    }
  }

  /**
   * Registers a {@code Lifecycle} instance.
   * 
   * @param lifecycleObject  the object that has a lifecycle, not null
   */
  private void registerLifecycle0(Lifecycle lifecycleObject) {
    _lifecycles.add(lifecycleObject);
  }

  //-------------------------------------------------------------------------
  /**
   * Registers a non-component object implementing {@code Lifecycle}.
   * 
   * @param servletContextAware  the object that requires a servlet context, not null
   */
  public void registerServletContextAware(ServletContextAware servletContextAware) {
    ArgumentChecker.notNull(servletContextAware, "servletContextAware");
    checkStatus(Status.CREATING);
    
    try {
      registerServletContextAware0(servletContextAware);
      registered(null, servletContextAware);
      
    } catch (RuntimeException ex) {
      _status.set(Status.FAILED);
      throw new RuntimeException("Failed during registering ServletContextAware: " + servletContextAware, ex);
    }
  }

  /**
   * Registers a {@code ServletContextAware} instance.
   * 
   * @param servletContextAware  the object that requires a servlet context, not null
   */
  private void registerServletContextAware0(ServletContextAware servletContextAware) {
    _servletContextAware.add(servletContextAware);
  }

  //-------------------------------------------------------------------------
  /**
   * Called whenever an instance is registered.
   * 
   * @param registeredKey  the key or info of the instance that was registered, null if not a component
   * @param registeredObject  the instance that was registered, may be null
   */
  protected void registered(Object registeredKey, Object registeredObject) {
    // override to handle event
    if (registeredKey instanceof ComponentInfo) {
      s_logger.info(" Registered component: {}", registeredKey);
    } else if (registeredKey instanceof ComponentKey) {
      s_logger.info(" Registered infrastructure: {}", registeredKey);
    } else {
      s_logger.info(" Registered: {}", registeredObject);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Marks this repository as complete and ready for use.
   */
  @Override
  public void start() {
    Status status = _status.get();
    if (status == Status.STARTING) {
      return;  // already starting
    }
    checkStatus(status, Status.CREATING);
    if (_status.compareAndSet(status, Status.STARTING) == false) {
      return;  // another thread just beat this one
    }
    try {
      for (Lifecycle obj : _lifecycles) {
        obj.start();
      }
      _status.set(Status.RUNNING);
      
    } catch (RuntimeException ex) {
      _status.set(Status.FAILED);
      throw ex;
    }
  }

  /**
   * Stops this repository.
   */
  @Override
  public void stop() {
    Status status = _status.get();
    if (status == Status.STOPPING || status == Status.STOPPED) {
      return;  // nothing to stop in this thread
    }
    if (_status.compareAndSet(status, Status.STOPPING) == false) {
      return;  // another thread just beat this one
    }
    for (Lifecycle obj : _lifecycles) {
      try {
        obj.stop();
      } catch (Exception ex) {
        // ignore
      }
    }
    _status.set(Status.STOPPED);
  }

  /**
   * Checks if this repository is running (started).
   * 
   * @return true if running
   */
  @Override
  public boolean isRunning() {
    return _status.get() == Status.RUNNING;
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the {@code ServletContext}.
   * 
   * @param servletContext  the servlet context, not null
   */
  @Override
  public void setServletContext(ServletContext servletContext) {
    servletContext.setAttribute(SERVLET_CONTEXT_KEY, this);
    for (ServletContextAware obj : _servletContextAware) {
      obj.setServletContext(servletContext);
    }
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

  /**
   * Gets the repository instance stored in the {@code ServletContext}.
   * <p>
   * This method should be used in preference to relying on the thread-local.
   * 
   * @param servletContext  the servlet context, not null
   * @return the instance stored in the servlet context, not null
   */
  public static ComponentRepository getFromServletContext(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    ComponentRepository repo = (ComponentRepository) servletContext.getAttribute(SERVLET_CONTEXT_KEY);
    ArgumentChecker.notNull(repo, "ComponentRepository");
    return repo;
  }

  //-------------------------------------------------------------------------
  private void checkStatus(Status required) {
    checkStatus(_status.get(), required);
  }

  private void checkStatus(Status current, Status required) {
    if (current != required) {
      throw new IllegalStateException("Invalid repository status, expected " + required + " but was " + current);
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
