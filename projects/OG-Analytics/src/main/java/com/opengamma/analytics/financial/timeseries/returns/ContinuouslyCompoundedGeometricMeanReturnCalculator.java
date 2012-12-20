/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ContinuouslyCompoundedGeometricMeanReturnCalculator extends Function1D<double[], Double> {

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final int n = x.length;
    double mult = Math.exp(x[0]);
    for (int i = 1; i < n; i++) {
      mult *= Math.exp(x[i]);
    }
    return Math.log(mult) / n;
  }
}
