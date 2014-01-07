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

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.target.Primitive.ExternalIdentifiablePrimitive;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
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
  
  @Test
  public void testExternalIdAsTarget() {
//    The following does not work
//    ExternalId extId = ExternalId.of(ExternalSchemes.ACTIVFEED_TICKER, "=SPX.W");
//    UniqueId uniqId = UniqueId.of(extId);
//  ExternalIdentifiablePrimitive primitive = new ExternalIdentifiablePrimitive(uniqId, extId);
    
    // But this does!
    UniqueId uniqId = UniqueId.of(ExternalSchemes.ACTIVFEED_TICKER.getName(), "=SPX.W");
    ExternalIdentifiablePrimitive primitive = new ExternalIdentifiablePrimitive(uniqId, uniqId.toExternalId());

    final FuturePriceCurveData<Double> data = new FuturePriceCurveData<Double>(DEFINITION_NAME, SPECIFICATION_NAME, primitive, X, VALUES);
    assertEquals(data, cycleObject(FuturePriceCurveData.class, data));
  }
}
