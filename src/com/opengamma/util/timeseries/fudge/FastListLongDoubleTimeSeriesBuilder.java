/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.mapping.FudgeBuilder;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries;

/**
 * 
 *
 * @author jim
 */
public class FastListLongDoubleTimeSeriesBuilder extends FastLongDoubleTimeSeriesBuilder<FastListLongDoubleTimeSeries> implements
    FudgeBuilder<FastListLongDoubleTimeSeries> {

  @Override
  public FastListLongDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, long[] times, double[] values) {
    return new FastListLongDoubleTimeSeries(encoding, times, values);
  }


}
