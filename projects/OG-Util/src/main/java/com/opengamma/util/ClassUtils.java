/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Class utilities
 */
public final class ClassUtils {

  /**
   * A cache of loaded classes.
   */
  private static final ConcurrentMap<String, Class<?>> s_classCache = new ConcurrentHashMap<>();
  /**
   * A cache of singletons.
   */
  private static final ConcurrentMap<Class<?>, Object> s_singletonCache = new ConcurrentHashMap<>();
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
   * @throws RuntimeException if the class cannot be found
   */
  public static Class<?> loadClassRuntime(String className) {
    try {
      return loadClass(className);
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
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
   * @param <T>  the type to cast to
   * @param className  the class name, not null
   * @param type  the type to cast to, not null
   * @return the class object, not null
   * @throws RuntimeException if the class cannot be found
   * @throws ClassCastException if the class is not a subtype of the specified type
   */
  public static <T> Class<? extends T> loadClassRuntime(String className, Class<T> type) {
    try {
      return loadClass(className).asSubclass(type);
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
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

  //-------------------------------------------------------------------------
  /**
   * Obtains the singleton instance of a type.
   * <p>
   * This finds and returns the singleton associated with a type.
   * 
   * @param <T>  the type
   * @param type  the type to find an instance for, not null
   * @return the singleton instance, not null
   */
  public static <T> T singletonInstance(Class<T> type) {
    Object result =  s_singletonCache.get(type);
    if (result == null) {
      result = singletonInstance0(type);
      s_singletonCache.putIfAbsent(type, result);
    }
    return type.cast(result);
  }

  private static <T> T singletonInstance0(Class<T> type) {
    try {
      Field field = type.getDeclaredField("INSTANCE");
      if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
        return type.cast(field.get(null));
      }
      Method method = type.getMethod("instance");
      if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
        return type.cast(method.invoke(null));
      }
      method = type.getMethod("getInstance");
      if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
        return type.cast(method.invoke(null));
      }
      throw new IllegalArgumentException("No suitable singleton found");
    } catch (ReflectiveOperationException ex) {
      throw new IllegalArgumentException("Exception while accessing singleton", ex);
    }
  }

}
