/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveData;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FuturePriceCurveDataFudgeEncodingTest extends FinancialTestBase {

  private static final String DEFINITION_NAME = "DN";
  private static final String SPECIFICATION_NAME = "SN";
  private static final Currency UID = Currency.USD;
  private static final Map<Double, Double> VALUES;
  private static final Double[] X;

  static {
    final int n = 10;
    X = new Double[n];
    VALUES = new HashMap<Double, Double>();
    for (int i = 0; i < n; i++) {
      X[i] = i * 3.;
      VALUES.put(X[i], Math.random());
    }
  }

  @Test
  public void testCycle() {
    final FuturePriceCurveData<Double> data = new FuturePriceCurveData<Double>(DEFINITION_NAME, SPECIFICATION_NAME, UID, X, VALUES);
    assertEquals(data, cycleObject(FuturePriceCurveData.class, data));
  }
}
