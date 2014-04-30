/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SABRATMVolatilityCalculatorTest {
  private static final SABRATMVolatilityCalculator CALCULATOR = new SABRATMVolatilityCalculator(new SABRHaganVolatilityFunction());
  private static final SABRFormulaData DATA = new SABRFormulaData(0.5, 1, 0.5, 0.3);
  private static final EuropeanVanillaOption OPTION = new EuropeanVanillaOption(100, 2, true);
  private static final double ATM_VOL = 0.24;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFormula() {
    new SABRATMVolatilityCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.calculate(null, OPTION, 100, ATM_VOL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption() {
    CALCULATOR.calculate(DATA, null, 100, ATM_VOL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeVol() {
    CALCULATOR.calculate(DATA, OPTION, 100, -ATM_VOL);
  }

  //TODO test result
}
