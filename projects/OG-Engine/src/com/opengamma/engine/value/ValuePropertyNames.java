/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

/**
 * Common value property names that are recognized by the engine. Additional names would be
 * specific to the financial integration library used to provide the functions.
 */
public final class ValuePropertyNames {

  /**
   * The function identifier. If there are multiple functions in a repository that can compute a given
   * value, this can allow a requirement to specify that a particular one be used.
   */
  public static final String FUNCTION = "function";

  private ValuePropertyNames() {
  }

}
