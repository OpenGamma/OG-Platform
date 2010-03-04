/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public abstract class IIDHypothesis extends Function1D<DoubleTimeSeries<?>, Boolean> {

  @Override
  public Boolean evaluate(final DoubleTimeSeries<?> x) {
    if (x == null)
      throw new IllegalArgumentException("Time series was null");
    if (x.isEmpty())
      throw new IllegalArgumentException("Time series was empty");
    return testIID(x);
  }

  public abstract boolean testIID(DoubleTimeSeries<?> x);
}
