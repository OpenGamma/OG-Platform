/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

/**
 * 
 */
public final class EmptyFunctionParameters implements FunctionParameters {

  /**
   * A default instance. Parameterizations will typically be used to customize behaviours of functions - the normal, default case is likely to be much more common so use this instead of allocating a
   * new instance whenever it is needed.
   */
  public static final EmptyFunctionParameters INSTANCE = new EmptyFunctionParameters();

  private static final long serialVersionUID = 1L;

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(final Object obj) {
    return (obj instanceof EmptyFunctionParameters);
  }

  @Override
  public String toString() {
    return "{}";
  }

}
