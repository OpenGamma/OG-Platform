/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import com.opengamma.util.timeseries.DoubleTimeSeriesTest;

public abstract class YearOffsetDoubleTimeSeriesTest extends DoubleTimeSeriesTest<Double> {

  @Override
  public Double[] testTimes() {
    return new Double[] {1d, 2d, 3d, 4d, 5d, 6d };
  }

  @Override
  public Double[] testTimes2() {
    return new Double[] {4d, 5d, 6d, 7d, 8d, 9d };
  }

  @Override
  public Double[] emptyTimes() {
    return new Double[] {};
  }

}
