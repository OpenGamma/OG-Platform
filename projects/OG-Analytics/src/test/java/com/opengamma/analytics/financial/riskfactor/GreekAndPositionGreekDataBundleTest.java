/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.financial.sensitivity.PositionGreek;
import com.opengamma.analytics.financial.trade.OptionTradeData;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GreekAndPositionGreekDataBundleTest {
  private static final GreekResultCollection GREEKS = new GreekResultCollection();
  private static final Map<PositionGreek, Double> RISK_FACTOR = new HashMap<>();
  private static final Map<UnderlyingType, Double> UNDERLYING = new HashMap<>();
  private static final double NUMBER_OF_CONTRACTS = 200;
  private static final double POINT_VALUE = 5;
  private static final OptionTradeData OPTION_TRADE_DATA = new OptionTradeData(NUMBER_OF_CONTRACTS, POINT_VALUE);
  private static final GreekDataBundle GREEKS_DATA;

  static {
    GREEKS.put(Greek.DELTA, 0.12);
    GREEKS.put(Greek.GAMMA, 0.34);
    RISK_FACTOR.put(new PositionGreek(Greek.DELTA), 12.);
    RISK_FACTOR.put(new PositionGreek(Greek.GAMMA), 34.);
    UNDERLYING.put(UnderlyingType.SPOT_PRICE, 40.);
    GREEKS_DATA = new GreekDataBundle(GREEKS, UNDERLYING, OPTION_TRADE_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGreeks() {
    new GreekDataBundle(null, UNDERLYING, OPTION_TRADE_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyGreeks() {
    new GreekDataBundle(new GreekResultCollection(), UNDERLYING, OPTION_TRADE_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    new GreekDataBundle(GREEKS, null, OPTION_TRADE_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData1() {
    new GreekDataBundle(GREEKS, new HashMap<UnderlyingType, Double>(), OPTION_TRADE_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGreekValueForGreek() {
    GREEKS_DATA.getGreekResultForGreek(Greek.CARRY_RHO);
  }

  @Test
  public void test() {
    assertEquals(GREEKS_DATA.getGreekResults(), GREEKS);
    assertEquals(GREEKS_DATA.getUnderlyingData(), UNDERLYING);
  }
}
