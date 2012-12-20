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
  
  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof EmptyFunctionParameters);
  }

  @Override
  public String toString() {
    return "{}";
  }

}
