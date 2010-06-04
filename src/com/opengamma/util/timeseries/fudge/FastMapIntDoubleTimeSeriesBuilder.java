/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.mapping.FudgeBuilder;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastMapIntDoubleTimeSeries;

/**
 * 
 *
 * @author jim
 */
public class FastMapIntDoubleTimeSeriesBuilder extends FastIntDoubleTimeSeriesBuilder<FastMapIntDoubleTimeSeries> implements
    FudgeBuilder<FastMapIntDoubleTimeSeries> {

  @Override
  public FastMapIntDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, int[] times, double[] values) {
    return new FastMapIntDoubleTimeSeries(encoding, times, values);
  }


}
