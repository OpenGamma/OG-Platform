/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Class utilities
 */
public final class ClassUtils {

  /**
   * A per-thread cache of loaded classes.
   */
  private static final ConcurrentMap<String, Class<?>> s_classCache = new ConcurrentHashMap<>();
  /**
   * Method for resolving a class.
   */
  private static final Method RESOLVE_METHOD;
  static {
    try {
      RESOLVE_METHOD = ClassLoader.class.getDeclaredMethod("resolveClass", Class.class);
      RESOLVE_METHOD.setAccessible(true);
    } catch (NoSuchMethodException | SecurityException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  /**
   * Prevents instantiation.
   */
  private ClassUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Loads a class from a class name, or fetches one from the calling thread's cache.
   * The calling thread's class loader is used.
   * <p>
   * Some class loaders involve quite heavy synchronization overheads which can impact
   * performance on multi-core systems if called heavy (for example as part of decoding a Fudge message).
   * <p>
   * The class will be fully initialized (static initializers invoked).
   * 
   * @param className  the class name, not null
   * @return the class object, not null
   * @throws ClassNotFoundException
   */
  public static Class<?> loadClass(String className) throws ClassNotFoundException {
    Class<?> clazz = s_classCache.get(className);
    if (clazz == null) {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (loader == null) {
        clazz = Class.forName(className);
      } else {
        clazz = Class.forName(className, true, loader);
      }
      s_classCache.putIfAbsent(className, clazz);
    }
    return clazz;
  }

  /**
   * Initializes a class to ensure it is fully loaded.
   * <p>
   * The JVM has two separate steps in class loading, the initial load
   * followed by the initialization.
   * Static initializers are invoked in the second step.
   * This method forces the second step.
   * 
   * @param <T>  the type
   * @param clazz  the class to initialize, not null
   * @return the input class, not null
   */
  public static <T> Class<T> initClass(Class<T> clazz) {
    String className = clazz.getName();
    if (s_classCache.containsKey(className) == false) {
      try {
        Class.forName(className, true, clazz.getClassLoader());
      } catch (ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException(ex.getMessage(), ex);
      }
      s_classCache.putIfAbsent(className, clazz);
    }
    return clazz;
  }

}
