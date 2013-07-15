/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import org.threeten.bp.Period;

import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class TenorLabelledLocalDateDoubleTimeSeriesMatrix1D extends LabelledObjectMatrix1D<Tenor, LocalDateDoubleTimeSeries, Period> {

  private static final Period TOLERANCE = Period.ofDays(1);
  
  public TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(final Tenor[] keys, final LocalDateDoubleTimeSeries[] values) {
    super(keys, values, TOLERANCE);
  }

  public TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(final Tenor[] keys, final Object[] labels, final LocalDateDoubleTimeSeries[] values) {
    super(keys, labels, values, TOLERANCE);
  }

  public TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(final Tenor[] keys, final Object[] labels, final String labelsTitle, final LocalDateDoubleTimeSeries[] values, final String valuesTitle) {
    super(keys, labels, labelsTitle, values, valuesTitle, TOLERANCE);
  }
  
  @Override
  public int compare(Tenor key1, Tenor key2, Period tolerance) {
    return LabelledMatrixUtils.compareTenorsWithTolerance(key1, key2, tolerance);
  }

}
