/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import org.apache.commons.lang.Validate;

/**
 * N-D function implementation, where N is specified in the constructor.
 * @param <S> Type of the arguments
 * @param <T> Return type of the function
 */
public abstract class FunctionND<S, T> implements Function<S, T> {
  private final int _dimension;
  
  /**
   * @param dimension The dimension of this function
   */
  public FunctionND(final int dimension) {
    Validate.isTrue(dimension > 0);
    _dimension = dimension;
  }

  /**
   * Implementation of the interface. 
   * @param x The list of inputs into the function, not null
   * @return The value of the function
   * @throws IllegalArgumentException If the number of arguments is not equal to the dimension
   */
  @Override
  public T evaluate(final S... x) {
    Validate.notNull(x, "x");
    if (x.length != _dimension) {
      throw new IllegalArgumentException("Number of variables " + x.length + " does not match dimension of function " + _dimension);
    }
    return evaluateFunction(x);
  }

  
  /**
   * @return The dimension of this function
   */
  public int getDimension() {
    return _dimension;
  }

  protected abstract T evaluateFunction(S[] x);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _dimension;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FunctionND<?, ?> other = (FunctionND<?, ?>) obj;
    return _dimension == other._dimension;
  }
  
}
