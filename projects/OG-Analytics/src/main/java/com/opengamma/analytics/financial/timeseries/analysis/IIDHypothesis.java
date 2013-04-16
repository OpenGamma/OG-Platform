/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public abstract class IIDHypothesis extends Function1D<DoubleTimeSeries<?>, Boolean> {

  @Override
  public Boolean evaluate(final DoubleTimeSeries<?> x) {
    Validate.notNull(x, "x");
    if (x.isEmpty()) {
      throw new IllegalArgumentException("Time series was empty");
    }
    return testIID(x);
  }

  public abstract boolean testIID(DoubleTimeSeries<?> x);
}
