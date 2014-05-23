/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskreward;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InformationRatioCalculatorTest {

  @Test
  public void test() {
    final double assetReturn = 0.12;
    final double benchmarkReturn = 0.03;
    final double assetStandardDeviation = 0.15;
    assertEquals(new InformationRatioCalculator().calculate(assetReturn, benchmarkReturn, assetStandardDeviation), 0.6, 0);
  }
}
