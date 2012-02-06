/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

/**
 * 
 */
public class StrikeAlgebra<T extends StrikeType> {

  @SuppressWarnings("unchecked")
  public T add(final T a, final T b) {
    double sum = a.value() + b.value();
    return (T) a.with(sum);
  }

  @SuppressWarnings("unchecked")
  public T subtract(final T a, final T b) {
    double diff = a.value() - b.value();
    return (T) a.with(diff);
  }

}
