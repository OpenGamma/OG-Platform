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
public final class InnerClassSubstitution {

  private InnerClassSubstitution() {
  }

  public static Method getMethod(final Object object) {
    try {
      return object.getClass().getMethod("writeReplace");
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("No serialization substitution available for anonymous inner class", e);
    }
  }

}
