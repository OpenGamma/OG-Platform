/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public abstract class HistoricalVaRCalculator {
  // TODO extract out the horizon scaling so that we can treat static and
  // dynamic portfolios correctly

  // public abstract Double scaleStatic();

  // public abstract Double scaleDynamic();

  public abstract Double evaluate(DoubleTimeSeries ts, double periods, double horizon, double quantile);
}
