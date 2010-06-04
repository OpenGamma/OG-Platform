/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.mapping.FudgeBuilder;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastListIntDoubleTimeSeries;

/**
 * 
 *
 * @author jim
 */
public class FastListIntDoubleTimeSeriesBuilder extends FastIntDoubleTimeSeriesBuilder<FastListIntDoubleTimeSeries> implements
    FudgeBuilder<FastListIntDoubleTimeSeries> {

  @Override
  public FastListIntDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, int[] times, double[] values) {
    return new FastListIntDoubleTimeSeries(encoding, times, values);
  }


}
