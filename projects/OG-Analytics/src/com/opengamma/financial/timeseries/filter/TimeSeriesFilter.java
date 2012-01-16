/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import cern.colt.Arrays;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public abstract class TimeSeriesFilter extends Function1D<LocalDateDoubleTimeSeries, FilteredTimeSeries> {

  protected FilteredTimeSeries getFilteredSeries(final LocalDateDoubleTimeSeries x, final int[] filteredDates, final double[] filteredData, final int i, final int[] rejectedDates,
      final double[] rejectedData, final int j) {
    
    FastArrayIntDoubleTimeSeries filtered = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, 
                                                                             (int[]) Arrays.trimToCapacity(filteredDates, i), 
                                                                             Arrays.trimToCapacity(filteredData, i));
    FastArrayIntDoubleTimeSeries rejected = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, 
                                                                             (int[]) Arrays.trimToCapacity(rejectedDates, j), 
                                                                             Arrays.trimToCapacity(rejectedData, j));
    return new FilteredTimeSeries(new ArrayLocalDateDoubleTimeSeries(filtered), 
                                  new ArrayLocalDateDoubleTimeSeries(rejected));
  }
}
