/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.serialization;

import java.lang.reflect.Method;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Allows anonymous inner classes to substitute themselves for one that can be inspected with
 * reflection. A {@code writeReplace} method is used on the inner class to share the same
 * substitution mechanism as if the class supports serialization. 
 */
public abstract class InnerClassSubstitution {

  /**
   * Returns the substitution object for the inner class if {@code writeReplace} is defined.
   * If there is no replacement method, the original object is returned.
   * 
   * @param object instance of an anonymous inner class
   * @return the substitution object or the original instance
   */
  public final Object getSubstitution(final Object object) {
    final Class<?> clazz = object.getClass();
    if (clazz.isAnonymousClass()) {
      try {
        return invoke(clazz.getMethod("writeReplace"), object);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("No serialization substitution available for anonymous inner class", e);
      }
    }
    return object;
  }

  protected abstract Object invoke(final Method method, final Object object) throws Exception;

}
