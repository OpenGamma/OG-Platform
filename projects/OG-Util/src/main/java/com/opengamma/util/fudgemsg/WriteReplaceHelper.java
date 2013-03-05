/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Helper methods for using the {@code writeReplace} method defined by Java serialization to allow an object to nominate another for encoding or serialization. For example, inner classes may implement
 * a {@code writeReplace} that allows them to be represented using an alternative strategy.
 */
public final class WriteReplaceHelper {

  private static final Object NO_WRITE_REPLACE = new Object();
  private static final Object BAD_ANONYMOUS_CLASS = new Object();

  /**
   * The cache of previously resolved (and forced accessible) {@code writeReplace} methods.
   */
  private static final ConcurrentMap<Class<?>, Object> s_writeReplace = new ConcurrentHashMap<Class<?>, Object>();

  private WriteReplaceHelper() {
  }

  private static Method getWriteReplace(final Class<?> clazz) {
    Object method = s_writeReplace.get(clazz);
    if (method == null) {
      method = AccessController.doPrivileged(new PrivilegedAction<Method>() {

        @Override
        public Method run() {
          try {
            final Method mtd = clazz.getMethod("writeReplace");
            mtd.setAccessible(true);
            return mtd;
          } catch (final NoSuchMethodException e) {
            // Ignore
          }
          return null;
        }

      });
      if (method == null) {
        if (clazz.isAnonymousClass()) {
          s_writeReplace.putIfAbsent(clazz, BAD_ANONYMOUS_CLASS);
          throw new OpenGammaRuntimeException("No serialization substitution available for anonymous inner class object " + clazz);
        } else {
          s_writeReplace.putIfAbsent(clazz, NO_WRITE_REPLACE);
          return null;
        }
      } else {
        s_writeReplace.putIfAbsent(clazz, method);
        return (Method) method;
      }
    } else {
      if (method == NO_WRITE_REPLACE) {
        return null;
      } else if (method == BAD_ANONYMOUS_CLASS) {
        throw new OpenGammaRuntimeException("No serialization substitution available for anonymous inner class object " + clazz);
      } else {
        return (Method) method;
      }
    }
  }

  /**
   * Replaces an class with a serializable substitution based on its {@code writeReplace} method.
   * 
   * @param object the object to substitute, not null
   * @return the substitution object as returned by its {@code writeReplace} method or the original object if there is no write replace
   * @throws OpenGammaRuntimeException if the class is an inner class and does not defined a {@code writeReplace}
   */
  public static Object writeReplace(final Object object) {
    final Class<?> clazz = object.getClass();
    final Method method = getWriteReplace(clazz);
    if (method == null) {
      return object;
    }
    try {
      return method.invoke(object);
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException("Couldn't call writeReplace on object", e);
    }
  }

}
