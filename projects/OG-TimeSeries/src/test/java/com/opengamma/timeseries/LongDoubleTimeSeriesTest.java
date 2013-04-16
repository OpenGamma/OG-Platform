/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * Test.
 */
public abstract class LongDoubleTimeSeriesTest extends DoubleTimeSeriesTest<Long> {

  @Override
  protected Long[] testTimes() {
    return new Long[] {1l, 2l, 3l, 4l, 5l, 6l };
  }

  @Override
  protected Long[] testTimes2() {
    return new Long[] {4l, 5l, 6l, 7l, 8l, 9l };
  }

  @Override
  protected Long[] emptyTimes() {
    return new Long[] {};
  }

}
