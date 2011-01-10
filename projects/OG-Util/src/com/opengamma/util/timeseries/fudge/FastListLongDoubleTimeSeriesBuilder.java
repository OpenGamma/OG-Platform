/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries;

/**
 */
@FudgeBuilderFor(FastListLongDoubleTimeSeries.class)
public class FastListLongDoubleTimeSeriesBuilder extends FastLongDoubleTimeSeriesBuilder<FastListLongDoubleTimeSeries> implements
    FudgeBuilder<FastListLongDoubleTimeSeries> {

  @Override
  public FastListLongDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, long[] times, double[] values) {
    return new FastListLongDoubleTimeSeries(encoding, times, values);
  }


}
