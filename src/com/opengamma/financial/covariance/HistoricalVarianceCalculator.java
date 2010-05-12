/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class HistoricalVarianceCalculator extends Function1D<Double, Double> {

  @Override
  public Double evaluate(final Double x) {
    return x * x;
  }

}
