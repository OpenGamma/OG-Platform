/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public abstract class SABRVolatilityFunctionTestCase {
  private static final double K = 105;
  private static final double T = 1.5;
  protected static final EuropeanVanillaOption OPTION = new EuropeanVanillaOption(K, T, true);
  protected static final SABRFormulaData LOG_NORMAL_EQUIVALENT = new SABRFormulaData(103, 0.8, 1, 0, 0.5);
  protected static final SABRFormulaData APPROACHING_LOG_NORMAL_EQUIVALENT1 = new SABRFormulaData(103, 0.8, 1, 1e-6, 0.5);
  protected static final SABRFormulaData APPROACHING_LOG_NORMAL_EQUIVALENT2 = new SABRFormulaData(103, 0.8, 1 + 1e-6, 0, 0.5);
  protected static final SABRFormulaData APPROACHING_LOG_NORMAL_EQUIVALENT3 = new SABRFormulaData(103, 0.8, 1 - 1e-6, 0, 0.5);

  protected abstract VolatilityFunctionProvider<SABRFormulaData> getFunction();

  @Test(expected = IllegalArgumentException.class)
  public void testNullOption() {
    getFunction().getVolatilityFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    getFunction().getVolatilityFunction(OPTION).evaluate((SABRFormulaData) null);
  }

  @Test
  public void testLogNormalEquivalent() {
    assertEquals(getFunction().getVolatilityFunction(OPTION).evaluate(LOG_NORMAL_EQUIVALENT), LOG_NORMAL_EQUIVALENT.getAlpha(), 0);
  }

  @Test
  public void testApproachingLogNormalEquivalent1() {
    assertEquals(getFunction().getVolatilityFunction(OPTION).evaluate(APPROACHING_LOG_NORMAL_EQUIVALENT1), LOG_NORMAL_EQUIVALENT.getAlpha(), 1e-5);
  }

  @Test
  public void testApproachingLogNormalEquivalent2() {
    assertEquals(getFunction().getVolatilityFunction(OPTION).evaluate(APPROACHING_LOG_NORMAL_EQUIVALENT2), LOG_NORMAL_EQUIVALENT.getAlpha(), 1e-5);
  }

  @Test
  public void testApproachingLogNormalEquivalent3() {
    assertEquals(getFunction().getVolatilityFunction(OPTION).evaluate(APPROACHING_LOG_NORMAL_EQUIVALENT3), LOG_NORMAL_EQUIVALENT.getAlpha(), 1e-5);
  }

  //TODO need to fill in tests
  //TODO beta = 1 nu = 0 => Black equivalent volatility
  //TODO beta = 0 nu = 0 => Bachelier
  //TODO beta != 0 nu = 0 => CEV
}
