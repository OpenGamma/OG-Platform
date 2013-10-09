/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
        clazz = loader.loadClass(className);
      }
      s_classCache.putIfAbsent(className, clazz);
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
   * @param <T>  the type
   * @param clazz  the class to initialize, not null
   * @return the input class, not null
   */
  @SuppressWarnings("restriction")
  public static <T> Class<T> initClass(Class<T> clazz) {
    UNSAFE.ensureClassInitialized(clazz);  // CSIGNORE
    return clazz;
  }

  //-------------------------------------------------------------------------
  /**
   * The unsafe object.
   */
  @SuppressWarnings("restriction")
  private static final sun.misc.Unsafe UNSAFE = findUnsafe();
  /**
   * Obtains an {@code Unsafe} object.
   * @return unsafe
   */
  @SuppressWarnings("restriction")
  private static sun.misc.Unsafe findUnsafe() {
    try {
      return sun.misc.Unsafe.getUnsafe();
    } catch (SecurityException ignored) {
      // ignore
    }
    try {
      return AccessController.doPrivileged(
          new PrivilegedExceptionAction<sun.misc.Unsafe>() {
            public sun.misc.Unsafe run() throws Exception {
              Class<sun.misc.Unsafe> unsafeClass = sun.misc.Unsafe.class;
              for (java.lang.reflect.Field f : unsafeClass.getDeclaredFields()) {
                if (unsafeClass.isAssignableFrom(f.getType())) {
                  f.setAccessible(true);
                  return unsafeClass.cast(f.get(null));
                }
              }
              throw new NoSuchFieldError("Unable to find Unsafe object");
            }
          });
    } catch (PrivilegedActionException ex) {
      throw new RuntimeException("Unable to find Unsafe object", ex.getCause());
    }
  }

}
