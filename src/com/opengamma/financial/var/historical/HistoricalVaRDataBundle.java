/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class HistoricalVaRDataBundle {
  private final DoubleTimeSeries<?> _pnl;

  public HistoricalVaRDataBundle(final DoubleTimeSeries<?> pnl) {
    Validate.notNull(pnl, "pnl series");
    _pnl = pnl;
  }

  public DoubleTimeSeries<?> getPNLSeries() {
    return _pnl;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_pnl == null) ? 0 : _pnl.hashCode());
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
    HistoricalVaRDataBundle other = (HistoricalVaRDataBundle) obj;
    return ObjectUtils.equals(_pnl, other._pnl);
  }

}
