/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class HistoricalVolatilityCloseCalculatorTest extends HistoricalVolatilityCalculatorTestCase {
  private static final HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR);

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new HistoricalVolatilityCloseCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new HistoricalVolatilityCloseCalculator(null, CalculationMode.LENIENT);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator3() {
    new HistoricalVolatilityCloseCalculator(null, CalculationMode.LENIENT, 0);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new DoubleTimeSeries[] {CLOSE_TS}), 0.0173, EPS);
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
