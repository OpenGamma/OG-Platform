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
