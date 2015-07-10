/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class SABRVolatilityFunctionTestCase {
  private static final double K = 105;
  private static final double T = 1.5;
  protected static final double FORWARD = 103;
  protected static final EuropeanVanillaOption OPTION = new EuropeanVanillaOption(K, T, true);
  protected static final SABRFormulaData LOG_NORMAL_EQUIVALENT = new SABRFormulaData(0.8, 1, 0.5, 0);
  protected static final SABRFormulaData APPROACHING_LOG_NORMAL_EQUIVALENT1 = new SABRFormulaData(0.8, 1, 0.5, 1e-6);
  protected static final SABRFormulaData APPROACHING_LOG_NORMAL_EQUIVALENT2 = new SABRFormulaData(0.8, 1 + 1e-6, 0.5, 0);
  protected static final SABRFormulaData APPROACHING_LOG_NORMAL_EQUIVALENT3 = new SABRFormulaData(0.8, 1 - 1e-6, 0.5, 0);

  protected abstract VolatilityFunctionProvider<SABRFormulaData> getFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption() {
    getFunction().getVolatilityFunction(null, FORWARD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    getFunction().getVolatilityFunction(OPTION, FORWARD).evaluate((SABRFormulaData) null);
  }

  @Test
  public void testLogNormalEquivalent() {
    assertEquals(getFunction().getVolatilityFunction(OPTION, FORWARD).evaluate(LOG_NORMAL_EQUIVALENT), LOG_NORMAL_EQUIVALENT.getAlpha(), 0);
  }

  @Test
  public void testApproachingLogNormalEquivalent1() {
    assertEquals(getFunction().getVolatilityFunction(OPTION, FORWARD).evaluate(APPROACHING_LOG_NORMAL_EQUIVALENT1), LOG_NORMAL_EQUIVALENT.getAlpha(), 1e-5);
  }

  @Test
  public void testApproachingLogNormalEquivalent2() {
    assertEquals(getFunction().getVolatilityFunction(OPTION, FORWARD).evaluate(APPROACHING_LOG_NORMAL_EQUIVALENT2), LOG_NORMAL_EQUIVALENT.getAlpha(), 1e-5);
  }

  @Test
  public void testApproachingLogNormalEquivalent3() {
    assertEquals(getFunction().getVolatilityFunction(OPTION, FORWARD).evaluate(APPROACHING_LOG_NORMAL_EQUIVALENT3), LOG_NORMAL_EQUIVALENT.getAlpha(), 1e-5);
  }

  //TODO need to fill in tests
  //TODO beta = 1 nu = 0 => Black equivalent volatility
  //TODO beta = 0 nu = 0 => Bachelier
  //TODO beta != 0 nu = 0 => CEV
}
