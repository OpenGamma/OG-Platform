/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.springframework.util.ClassUtils;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Utility to provide reflection helpers.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class ReflectionUtils {

  /**
   * Restricted constructor.
   */
  private ReflectionUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Loads a Class from a full class name.
   * <p>
   * This uses Spring's {@link ClassUtils#forName(String, ClassLoader)} passing null
   * for the class loader.
   * 
   * @param <T> the auto-cast class
   * @param className  the class name, not null
   * @return the class, not null
   * @throws RuntimeException if the class cannot be loaded
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> loadClass(final String className) {
    try {
      return (Class<T>) ClassUtils.forName(className, null);
    } catch (ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
  }

  /**
   * Loads a Class from a full class name with a fallback class loader.
   * <p>
   * This uses Spring's {@link ClassUtils#forName(String, ClassLoader)} passing null
   * for the class loader. If that fails, it calls the same method with the class loader
   * 
   * @param <T> the auto-cast class
   * @param className  the class name, not null
   * @param fallbackClassLoader  a suitable class loader, may be null
   * @return the class, not null
   * @throws RuntimeException if the class cannot be loaded
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> loadClassWithFallbackLoader(final String className, final ClassLoader fallbackClassLoader) {
    try {
      return (Class<T>) ClassUtils.forName(className, null);
    } catch (ClassNotFoundException ex) {
      try {
        return (Class<T>) ClassUtils.forName(className, fallbackClassLoader);
      } catch (ClassNotFoundException ex2) {
        throw new OpenGammaRuntimeException(ex.getMessage(), ex2);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a constructor from a Class.
   * 
   * @param <T> the type
   * @param type  the type to create, not null
   * @param paramTypes  the parameter types, not null
   * @return the constructor, not null
   * @throws RuntimeException if the class cannot be loaded
   */
  public static <T> Constructor<T> findConstructor(final Class<T> type, final Class<?>... paramTypes) {
    try {
      return type.getConstructor(paramTypes);
    } catch (NoSuchMethodException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance of a class from a constructor..
   * 
   * @param <T> the type
   * @param constructor  the constructor to call, not null
   * @param args  the arguments, not null
   * @return the constructor, not null
   * @throws RuntimeException if the class cannot be loaded
   */
  public static <T> T newInstance(final Constructor<T> constructor, final Object... args) {
    try {
      return constructor.newInstance(args);
    } catch (InstantiationException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    } catch (IllegalAccessException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    } catch (InvocationTargetException ex) {
      if (ex.getCause() instanceof RuntimeException) {
        throw (RuntimeException) ex.getCause();
      }
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
  }

}
