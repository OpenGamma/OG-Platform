/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the Normal option pricing data sets.
 */
@Test(groups = TestGroup.UNIT)
public class NormalFunctionDataTest {

  private static final double FORWARD = 0.05;
  private static final double NUMERAIRE = 0.95;
  private static final double VOLATILITY = 0.01;
  private static final NormalFunctionData NORMAL_DATA = new NormalFunctionData(FORWARD, NUMERAIRE, VOLATILITY);

  @Test
  public void getter() {
    assertEquals("NormalFunctionData: getter", FORWARD, NORMAL_DATA.getForward());
    assertEquals("NormalFunctionData: getter", NUMERAIRE, NORMAL_DATA.getNumeraire());
    assertEquals("NormalFunctionData: getter", VOLATILITY, NORMAL_DATA.getNormalVolatility());
  }

}
