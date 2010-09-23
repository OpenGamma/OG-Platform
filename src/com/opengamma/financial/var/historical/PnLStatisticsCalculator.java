/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class PnLStatisticsCalculator extends Function1D<HistoricalVaRDataBundle, Double> {
  private final Function<DoubleTimeSeries<?>, Double> _calculator;

  public PnLStatisticsCalculator(final Function<DoubleTimeSeries<?>, Double> calculator) {
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PnLStatisticsCalculator other = (PnLStatisticsCalculator) obj;
    return ObjectUtils.equals(_calculator, other._calculator);
  }

}
