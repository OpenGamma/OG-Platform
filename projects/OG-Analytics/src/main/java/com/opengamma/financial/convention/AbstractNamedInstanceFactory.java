/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClassUtils;

/**
 * An abstract factory for named instances.
 * <p>
 * A named instance is a type where each instance is uniquely identified by a name.
 * This factory provides access to all the instances.
 * <p>
 * Implementations should typically be singletons with a public static factory instance
 * named 'INSTANCE' accessible using {@link ClassUtils#singletonInstance(Class)}.
 * 
 * @param <T> type of objects returned
 */
public abstract class AbstractNamedInstanceFactory<T extends NamedInstance>
    implements NamedInstanceFactory<T> {

  /**
   * The named instance type.
   */
  private final Class<T> _type;
  /**
   * Map of primary instances.
   */
  private final ConcurrentMap<String, T> _instanceMap = Maps.newConcurrentMap();
  /**
   * Map of all instances.
   */
  private final ConcurrentMap<String, T> _instanceMapAltNames = Maps.newConcurrentMap();
  /**
   * Lookup map of instances keyed by lower case.
   */
  private final ConcurrentMap<String, T> _lookupMap = Maps.newConcurrentMap();

  /**
   * Creates the factory.
   * 
   * @param type  the type of named instance, not null
   */
  protected AbstractNamedInstanceFactory(Class<T> type) {
    _type = ArgumentChecker.notNull(type, "type");
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an instance, potentially using a different name.
   * 
   * @param instance  the named instance, not null
   * @param alternativeNames  the alternative names to use in addition to the instance name, not null
   * @return the instance, not null
   */
  protected T addInstance(T instance, String... alternativeNames) {
    ArgumentChecker.notNull(instance, "instance");
    ArgumentChecker.notNull(alternativeNames, "alternativeNames");
    _instanceMap.put(instance.getName(), instance);
    _instanceMapAltNames.put(instance.getName(), instance);
    _lookupMap.put(instance.getName().toLowerCase(Locale.ENGLISH), instance);
    for (String altName : alternativeNames) {
      _instanceMapAltNames.put(altName, instance);
      _lookupMap.put(altName.toLowerCase(Locale.ENGLISH), instance);
    }
    return instance;
  }

  /**
   * Loads instances from a properties file based on the type.
   * <p>
   * The properties file must be a name key to a class name.
   */
  protected void loadFromProperties() {
    loadFromProperties(_type.getName());
  }

  /**
   * Loads instances from a properties file.
   * <p>
   * The properties file must be a name key to a class name.
   * 
   * @param bundleName  the bundle name, not null
   */
  protected void loadFromProperties(String bundleName) {
    ArgumentChecker.notNull(bundleName, "bundleName");
    final ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
    final Map<String, T> instances = Maps.newHashMap();
    for (String name : bundle.keySet()) {
      String implementationType = bundle.getString(name);
      T instance = instances.get(implementationType);
      if (instance == null) {
        try {
          instance = ClassUtils.loadClassRuntime(implementationType).asSubclass(_type).newInstance();
          instances.put(implementationType, instance);
        } catch (Exception ex) {
          throw new OpenGammaRuntimeException("Error loading properties for " + _type.getSimpleName(), ex);
        }
      }
      addInstance(instance, name);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public T instance(final String name) {
    ArgumentChecker.notNull(name, "name");
    final T result = _lookupMap.get(name.toLowerCase(Locale.ENGLISH));
    if (result == null) {
      throw new IllegalArgumentException("Unknown " + _type.getSimpleName() + ": " + name);
    }
    return result;
  }

  @Override
  public Map<String, T> instanceMap() {
    return Collections.unmodifiableMap(_instanceMap);
  }

  @Override
  public Map<String, T> instanceMapWithAlternateNames() {
    return Collections.unmodifiableMap(_instanceMapAltNames);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "NamedInstanceFactory[" + _type.getSimpleName() + ",size=" + _instanceMapAltNames.size() + "]";
  }

}
