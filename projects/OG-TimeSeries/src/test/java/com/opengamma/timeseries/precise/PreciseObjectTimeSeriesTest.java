/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.timeseries.BigDecimalObjectTimeSeriesTest;

/**
 * Abstract test class for {@code PreciseObjectTimeSeries}.
 * 
 * @param <T>  the time type
 */
@Test(groups = "unit")
public abstract class PreciseObjectTimeSeriesTest<T> extends BigDecimalObjectTimeSeriesTest<T> {

  @Test
  public void test_intersectionFirstValue() {
    PreciseObjectTimeSeries<T, BigDecimal> dts = (PreciseObjectTimeSeries<T, BigDecimal>) createStandardTimeSeries();
    PreciseObjectTimeSeries<T, BigDecimal> dts2 = (PreciseObjectTimeSeries<T, BigDecimal>) createStandardTimeSeries2();
    PreciseObjectTimeSeries<T, BigDecimal> ets = (PreciseObjectTimeSeries<T, BigDecimal>) createEmptyTimeSeries();
    assertEquals(ets, ets.intersectionFirstValue(dts));
    assertEquals(ets, dts.intersectionFirstValue(ets));
    
    PreciseObjectTimeSeries<T, BigDecimal> result = dts.intersectionFirstValue(dts2);
    assertEquals(3, result.size());
    assertEquals(testValues()[3], result.getValueAtIndex(0));
    assertEquals(testValues()[4], result.getValueAtIndex(1));
    assertEquals(testValues()[5], result.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result.getTimeAtIndex(2));
  }

  @Test
  public void test_noIntersectionOperation() {
    PreciseObjectTimeSeries<T, BigDecimal> dts = (PreciseObjectTimeSeries<T, BigDecimal>) createStandardTimeSeries();
    PreciseObjectTimeSeries<T, BigDecimal> dts2 = (PreciseObjectTimeSeries<T, BigDecimal>) createStandardTimeSeries2();
    PreciseObjectTimeSeries<T, BigDecimal> ets = (PreciseObjectTimeSeries<T, BigDecimal>) createEmptyTimeSeries();
    assertEquals(dts, ets.noIntersectionOperation(dts));
    assertEquals(dts, dts.noIntersectionOperation(ets));
    try {
      dts.noIntersectionOperation(dts2);
      fail("Should have failed");
    } catch (IllegalStateException ex) {
      //do nothing - expected exception because the two timeseries have overlapping dates which will require intersection operation
    }
    PreciseObjectTimeSeries<T, BigDecimal> dts3 = dts2.subSeries(dts.getLatestTime(), false, dts2.getLatestTime(), false);
    PreciseObjectTimeSeries<T, BigDecimal> noIntersecOp = dts.noIntersectionOperation(dts3);
    assertEquals(dts.getValueAtIndex(0), noIntersecOp.getValueAtIndex(0));
    assertEquals(dts.getValueAtIndex(1), noIntersecOp.getValueAtIndex(1));
    assertEquals(dts.getValueAtIndex(2), noIntersecOp.getValueAtIndex(2));
    assertEquals(dts.getValueAtIndex(3), noIntersecOp.getValueAtIndex(3));
    assertEquals(dts.getValueAtIndex(4), noIntersecOp.getValueAtIndex(4));
    assertEquals(dts.getValueAtIndex(5), noIntersecOp.getValueAtIndex(5));
    assertEquals(dts3.getValueAtIndex(0), noIntersecOp.getValueAtIndex(6));
    assertEquals(dts3.getValueAtIndex(1), noIntersecOp.getValueAtIndex(7));
  }

}
