/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Map;

import com.opengamma.util.ClassUtils;

/**
 * An interface for named instances.
 * <p>
 * A named instance is a type where each instance is uniquely identified by a name.
 * This factory provides access to all the instances.
 * <p>
 * Implementations should typically be singletons with a public static factory instance
 * named 'INSTANCE' accessible using {@link ClassUtils#singletonInstance(Class)}.
 * 
 * @param <T> type of objects returned
 */
public interface NamedInstanceFactory<T extends NamedInstance> {

  /**
   * Finds a named instance by name, ignoring case.
   * 
   * @param name  the name of the instance to find, not null
   * @return the named instance, not null
   * @throws IllegalArgumentException if the name is not found
   */
  T instance(String name);

  /**
   * Returns the map of available instances keyed by name, excluding alternate names.
   * <p>
   * A named instance may be registered under more than one name.
   * Those additional names are excluded.
   * 
   * @return the unmodifiable map of named instances, not null
   */
  Map<String, T> instanceMap();

  /**
   * Returns the map of available instances keyed by name, including alternate names.
   * <p>
   * A named instance may be registered under more than one name.
   * Those additional names are included.
   * 
   * @return the unmodifiable map of named instances, not null
   */
  Map<String, T> instanceMapWithAlternateNames();

}
