/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalVolatilityCloseCalculatorTest extends HistoricalVolatilityCalculatorTestCase {
  private static final HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new HistoricalVolatilityCloseCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new HistoricalVolatilityCloseCalculator(null, CalculationMode.LENIENT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator3() {
    new HistoricalVolatilityCloseCalculator(null, CalculationMode.LENIENT, 0);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {CLOSE_TS}), 0.0173, EPS);
  }

  @Override
  protected HistoricalVolatilityCalculator getCalculator() {
    return CALCULATOR;
  }

  @Test
  public void testObject() {
    HistoricalVolatilityCalculator other = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR);
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR, CalculationMode.STRICT);
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR, CalculationMode.STRICT, 0.0);
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = new HistoricalVolatilityCloseCalculator(RELATIVE_RETURN_CALCULATOR, CalculationMode.STRICT, 0.0);
    assertFalse(CALCULATOR.equals(other));
    other = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR, CalculationMode.LENIENT, 0.0);
    assertFalse(CALCULATOR.equals(other));
    other = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR, CalculationMode.STRICT, 0.001);
    assertFalse(CALCULATOR.equals(other));
  }
}
