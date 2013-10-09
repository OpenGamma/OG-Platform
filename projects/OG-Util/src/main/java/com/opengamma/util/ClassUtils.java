/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Class utilities
 */
public final class ClassUtils {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ClassUtils.class);
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

  /**
   * Loads a class from a class name, or fetches one from the calling thread's cache.
   * The calling thread's class loader is used.
   * <p>
   * Some class loaders involve quite heavy synchronization overheads which can impact
   * performance on multi-core systems if called heavy (for example as part of decoding a Fudge message).
   * <p>
   * The class will be fully initialized (static initializers invoked).
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
      initClass(clazz);
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
   * @param clazz  the class to initialize, not null
   */
  public static void initClass(Class<?> clazz) {
    try {
      RESOLVE_METHOD.invoke(clazz.getClassLoader(), clazz);
    } catch (InvocationTargetException ex) {
      Throwable cause = (ex.getCause() != null ? ex.getCause() : ex);
      throw Throwables.propagate(cause);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      s_logger.error("Unable to initialize class", ex);
    }
  }

}
