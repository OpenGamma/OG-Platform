/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.longint.FastMapLongDoubleTimeSeries;

/**
 */
@FudgeBuilderFor(FastMapLongDoubleTimeSeries.class)
public class FastMapLongDoubleTimeSeriesFudgeBuilder extends FastLongDoubleTimeSeriesFudgeBuilder<FastMapLongDoubleTimeSeries> implements
    FudgeBuilder<FastMapLongDoubleTimeSeries> {

  @Override
  public FastMapLongDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, long[] times, double[] values) {
    return new FastMapLongDoubleTimeSeries(encoding, times, values);
  }


}
