/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Class utilities
 */
public final class ClassUtils {

  /**
   * A per-thread cache of loaded classes.
   */
  private static final ThreadLocal<Map<String, Class<?>>> s_classCache = new ThreadLocal<Map<String, Class<?>>>() {
    @Override
    protected Map<String, Class<?>> initialValue() {
      return new HashMap<String, Class<?>>();
    }
  };

  /**
   * Prevents instantiation.
   */
  private ClassUtils() {
  }

  /**
   * Loads a class from a class name, or fetches one from the calling thread's cache. The calling thread's class loader is used.
   * <p>
   * Some class loaders involve quite heavy synchronization overheads which can impact performance on multi-core systems if called heavy (for example as part of decoding a Fudge message).
   * 
   * @param className the class name, not null
   * @return the class object, not null
   * @throws ClassNotFoundException
   */
  public static Class<?> loadClass(String className) throws ClassNotFoundException {
    final Map<String, Class<?>> loaded = s_classCache.get();
    Class<?> clazz = loaded.get(className);
    if (clazz == null) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (loader == null) {
        clazz = Class.forName(className);
      } else {
        clazz = loader.loadClass(className);
      }
      loaded.put(className, clazz);
    }
    return clazz;
  }

}
