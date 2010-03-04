/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ExponentialWeightedMovingAverageHistoricalVolatilityCalculatorTest extends HistoricalVolatilityCalculatorTestCase {
  private static final HistoricalVolatilityCalculator CALCULATOR = new ExponentialWeightedMovingAverageHistoricalVolatilityCalculator(0.94, RETURN_CALCULATOR);

  @Test
  public void test() {
    assertEquals(Math.sqrt(252) * CALCULATOR.evaluate(CLOSE_TS), 0.2455, EPS);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.covariance.HistoricalVolatilityCalculatorTestCase
   * #getCalculator()
   */
  @Override
  protected HistoricalVolatilityCalculator getCalculator() {
    return CALCULATOR;
  }
}
