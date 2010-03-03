/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import cern.colt.Arrays;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public abstract class TimeSeriesFilter<T extends DoubleTimeSeries<?>> extends Function1D<T, FilteredTimeSeries<DoubleTimeSeries<Long>>> {

  protected FilteredTimeSeries<DoubleTimeSeries<Long>> getFilteredSeries(final FastLongDoubleTimeSeries x, final long[] filteredDates, final double[] filteredData, final int i,
      final long[] rejectedDates, final double[] rejectedData, final int j) {
    final DateTimeNumericEncoding encoding = x.getEncoding();
    return new FilteredTimeSeries<DoubleTimeSeries<Long>>(new FastArrayLongDoubleTimeSeries(encoding, Arrays.trimToCapacity(filteredDates, i), Arrays.trimToCapacity(filteredData,
        i)), new FastArrayLongDoubleTimeSeries(encoding, Arrays.trimToCapacity(rejectedDates, j), Arrays.trimToCapacity(rejectedData, j)));
  }
}
