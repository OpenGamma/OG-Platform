/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Contains utility methods for checking inputs to methods.
 *
 * @author kirk
 */
public final class ArgumentChecker {
  private ArgumentChecker() {
  }
  
  public static void checkNotNull(Object parameter, String name) throws NullPointerException {
    if(parameter == null) {
      throw new NullPointerException("Input parameter " + name + " must not be null.");
    }
  }
  
  public static void checkNotNullInjected(Object parameter, String name) throws NullPointerException {
    if(parameter == null) {
      throw new NullPointerException("Injected input parameter " + name + " must not be null.");
    }
  }

}
