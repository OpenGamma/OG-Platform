/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.timeseries.returns.SimpleNetRelativeTimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.SimpleNetTimeSeriesReturnCalculator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalVolatilityHighLowCloseCalculatorTest extends HistoricalVolatilityCalculatorTestCase {
  private static final HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, RELATIVE_RETURN_CALCULATOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new HistoricalVolatilityHighLowCloseCalculator(null, RELATIVE_RETURN_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new HistoricalVolatilityHighLowCloseCalculator(null, RELATIVE_RETURN_CALCULATOR, CalculationMode.LENIENT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator3() {
    new HistoricalVolatilityHighLowCloseCalculator(null, RELATIVE_RETURN_CALCULATOR, CalculationMode.LENIENT, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator4() {
    new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator5() {
    new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, null, CalculationMode.LENIENT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator6() {
    new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, null, CalculationMode.LENIENT, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTS() {
    CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {HIGH_TS, LOW_TS});
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {HIGH_TS, LOW_TS, CLOSE_TS}), 0.0128, EPS);
  }

  @Override
  protected HistoricalVolatilityCalculator getCalculator() {
    return CALCULATOR;
  }

  @Test
  public void testObject() {
    HistoricalVolatilityCalculator other = new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, RELATIVE_RETURN_CALCULATOR);
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, RELATIVE_RETURN_CALCULATOR, CalculationMode.STRICT);
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, RELATIVE_RETURN_CALCULATOR, CalculationMode.STRICT, 0.0);
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, new SimpleNetRelativeTimeSeriesReturnCalculator(CalculationMode.LENIENT), CalculationMode.STRICT, 0.0);
    assertFalse(CALCULATOR.equals(other));
    other = new HistoricalVolatilityHighLowCloseCalculator(new SimpleNetTimeSeriesReturnCalculator(CalculationMode.LENIENT), RELATIVE_RETURN_CALCULATOR, CalculationMode.STRICT, 0.0);
    assertFalse(CALCULATOR.equals(other));
    other = new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, RELATIVE_RETURN_CALCULATOR, CalculationMode.LENIENT, 0.0);
    assertFalse(CALCULATOR.equals(other));
    other = new HistoricalVolatilityHighLowCloseCalculator(RETURN_CALCULATOR, RELATIVE_RETURN_CALCULATOR, CalculationMode.STRICT, 0.001);
    assertFalse(CALCULATOR.equals(other));
  }
}
