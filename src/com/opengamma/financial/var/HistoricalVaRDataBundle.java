/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * @author emcleod
 * 
 */
public class HistoricalVaRDataBundle {
  private final DoubleTimeSeries _pnl;

  public HistoricalVaRDataBundle(final DoubleTimeSeries pnl) {
    if (pnl == null)
      throw new IllegalArgumentException("P&L time series was empty");
    _pnl = pnl;
  }

  public DoubleTimeSeries getPNLSeries() {
    return _pnl;
  }
}
