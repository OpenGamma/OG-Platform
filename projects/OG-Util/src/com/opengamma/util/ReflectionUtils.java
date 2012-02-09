/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.Closeable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
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
   * @param arguments  the arguments, not null
   * @return the constructor, not null
   * @throws RuntimeException if the class cannot be loaded
   */
  @SuppressWarnings("unchecked")
  public static <T> Constructor<T> findConstructorByArguments(final Class<T> type, final Object... arguments) {
    Class<?>[] paramTypes = new Class<?>[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      paramTypes[i] = (arguments[i] != null ? arguments[i].getClass() : null);
    }
    List<Constructor<?>> constructors = Arrays.asList(type.getConstructors());
    for (Iterator<Constructor<?>> it = constructors.iterator(); it.hasNext(); ) {
      Constructor<?> constructor = (Constructor<?>) it.next();
      if (org.apache.commons.lang.ClassUtils.isAssignable(paramTypes, constructor.getParameterTypes()) == false) {
        it.remove();
      }
    }
    if (constructors.size() != 1) {
      throw new OpenGammaRuntimeException("Unable to match single constructor: " + type);
    }
    return (Constructor<T>) constructors.get(0);
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
   * Creates an instance of a class from a constructor.
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

  //-------------------------------------------------------------------------
  /**
   * Checks if the class is closeable.
   * <p>
   * This invokes the close method if it is present.
   * 
   * @param type  the type, not null
   * @return true if closeable
   */
  public static boolean isCloseable(final Class<?> type) {
    if (Closeable.class.isAssignableFrom(type)) {
      return true;
    } else if (DisposableBean.class.isAssignableFrom(type)) {
      return true;
    }
    try {
      if (Modifier.isPublic(type.getMethod("close").getModifiers())) {
        return true;
      }
    } catch (Exception ex) {
      try {
        if (Modifier.isPublic(type.getMethod("shutdown").getModifiers())) {
          return true;
        }
      } catch (Exception ex2) {
        // ignored
      }
    }
    return false;
  }

  /**
   * Tries to "close" an object.
   * <p>
   * This invokes the close method if it is present.
   * 
   * @param obj  the object, null ignored
   */
  public static void close(final Object obj) {
    if (obj != null) {
      try {
        if (obj instanceof Closeable) {
          ((Closeable) obj).close();
        } else if (obj instanceof DisposableBean) {
          ((DisposableBean) obj).destroy();
        } else {
          invokeNoArgsNoException(obj, "close");
          invokeNoArgsNoException(obj, "shutdown");
        }
      } catch (Exception ex) {
        // ignored
      }
    }
  }

  /**
   * Invokes a no-args method on an object, throwing no errors.
   * 
   * @param obj  the object, null ignored
   * @param methodName  the method name, not null
   */
  public static void invokeNoArgsNoException(final Object obj, final String methodName) {
    if (obj != null) {
      try {
        obj.getClass().getMethod(methodName).invoke(obj);
      } catch (Exception ex2) {
        // ignored
      }
    }
  }

}
