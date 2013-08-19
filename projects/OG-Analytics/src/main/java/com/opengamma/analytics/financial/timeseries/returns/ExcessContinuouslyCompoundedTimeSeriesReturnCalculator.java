/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;

/**
 * The excess return of an asset at time <i>t</i> is the difference between the
 * return of that asset and the return of a reference asset. This class
 * calculates the excess continuously-compounded return.
 */
public class ExcessContinuouslyCompoundedTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {
  private final Function<LocalDateDoubleTimeSeries, LocalDateDoubleTimeSeries> _returnCalculator;

  public ExcessContinuouslyCompoundedTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
    _returnCalculator = new ContinuouslyCompoundedTimeSeriesReturnCalculator(mode);
  }

  /**
   * @param x
   *          An array of DoubleTimeSeries. The series <b>must</b> contain at
   *          least four elements; the asset price series, the dividend price
   *          series (can be null but it must be the second element), the
   *          reference price series and the reference dividend series. Any
   *          further elements will be ignored.
   * @throws IllegalArgumentException
   *          If the array is null
   * @throws TimeSeriesException
   *           Throws an exception if: the array has less
   *           than two elements; the calculation mode is strict and the price
   *           series are not the same length.
   * @return A DoubleTimeSeries containing the excess return series.
   */
  @Override
  public LocalDateDoubleTimeSeries evaluate(final LocalDateDoubleTimeSeries... x) {
    Validate.notNull(x, "x");
    if (x.length < 4) {
      throw new TimeSeriesException("Time series array must contain at least four elements");
    }
    if (getMode() == CalculationMode.STRICT && x[0].size() != x[2].size()) {
      throw new TimeSeriesException("Asset price series and reference price series were not the same size");
    }
    final LocalDateDoubleTimeSeries assetReturn = x[1] == null ? _returnCalculator.evaluate(x[0]) : _returnCalculator.evaluate(x[0], x[1]);
    final LocalDateDoubleTimeSeries referenceReturn = x[3] == null ? _returnCalculator.evaluate(x[2]) : _returnCalculator.evaluate(x[2], x[3]);
    return assetReturn.subtract(referenceReturn);
  }
}
