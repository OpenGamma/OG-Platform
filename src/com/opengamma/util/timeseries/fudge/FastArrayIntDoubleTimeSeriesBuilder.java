/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.mapping.FudgeBuilder;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 *
 * @author jim
 */
public class FastArrayIntDoubleTimeSeriesBuilder extends FastIntDoubleTimeSeriesBuilder<FastArrayIntDoubleTimeSeries> implements
    FudgeBuilder<FastArrayIntDoubleTimeSeries> {
  
  public FastArrayIntDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, int[] times, double[] values) {
    return new FastArrayIntDoubleTimeSeries(encoding, times, values);
  }

}
