/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * PLAT-1590
 */
public final class TimeSeriesIntersector {

  public static DoubleTimeSeries<?>[] intersect(DoubleTimeSeries<?>... series) {
    if (series.length <= 1) {
      return series;
    }
    
    //Make the smallest series we can
    for (int i = 1; i < series.length; i++) {
      series[0] = series[0].intersectionFirstValue(series[i]);
    }
    //Shrink everything else
    for (int i = 1; i < series.length; i++) {
      series[i] = series[i].intersectionFirstValue(series[0]);
    }
    return series;
  }
}
