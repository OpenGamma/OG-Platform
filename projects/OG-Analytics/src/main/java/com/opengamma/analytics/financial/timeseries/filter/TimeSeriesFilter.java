/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import cern.colt.Arrays;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public abstract class TimeSeriesFilter extends Function1D<LocalDateDoubleTimeSeries, FilteredTimeSeries> {

  protected FilteredTimeSeries getFilteredSeries(final int[] filteredDates, final double[] filteredData, final int i, final int[] rejectedDates, final double[] rejectedData, final int j) {
    final LocalDateDoubleTimeSeries filtered = ImmutableLocalDateDoubleTimeSeries.of(Arrays.trimToCapacity(filteredDates, i), Arrays.trimToCapacity(filteredData, i));
    final LocalDateDoubleTimeSeries rejected = ImmutableLocalDateDoubleTimeSeries.of(Arrays.trimToCapacity(rejectedDates, j), Arrays.trimToCapacity(rejectedData, j));
    return new FilteredTimeSeries(filtered, rejected);
  }

}
