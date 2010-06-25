/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class PnLStatisticsCalculator extends Function1D<HistoricalVaRDataBundle, Double> {
  private final Function1D<DoubleTimeSeries<?>, Double> _calculator;

  public PnLStatisticsCalculator(final Function1D<DoubleTimeSeries<?>, Double> calculator) {
    Validate.notNull(calculator, "calculator");
    _calculator = calculator;
  }

  @Override
  public Double evaluate(final HistoricalVaRDataBundle data) {
    Validate.notNull(data, "data");
    return _calculator.evaluate(data.getPNLSeries());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_calculator == null) ? 0 : _calculator.hashCode());
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
    PnLStatisticsCalculator other = (PnLStatisticsCalculator) obj;
    if (_calculator == null) {
      if (other._calculator != null) {
        return false;
      }
    } else if (!_calculator.equals(other._calculator)) {
      return false;
    }
    return true;
  }

}
