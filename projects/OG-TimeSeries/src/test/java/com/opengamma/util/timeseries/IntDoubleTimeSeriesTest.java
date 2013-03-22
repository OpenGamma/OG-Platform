/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

public abstract class IntDoubleTimeSeriesTest extends DoubleTimeSeriesTest<Integer> {

  @Override
  protected Integer[] testTimes() {
    return new Integer[] {1, 2, 3, 4, 5, 6 };
  }

  @Override
  protected Integer[] testTimes2() {
    return new Integer[] {4, 5, 6, 7, 8, 9 };
  }

  @Override
  protected Integer[] emptyTimes() {
    return new Integer[] {};
  }

}
