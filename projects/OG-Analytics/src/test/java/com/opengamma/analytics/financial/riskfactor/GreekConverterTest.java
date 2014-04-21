/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.financial.sensitivity.PositionGreek;
import com.opengamma.analytics.financial.sensitivity.ValueGreek;
import com.opengamma.analytics.financial.trade.OptionTradeData;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GreekConverterTest {
  private static final double N = 123.;
  private static final double PV = 456;
  private static final double DELTA = 0.12;
  private static final double GAMMA = 0.34;
  private static final double VANNA = 0.56;
  private static final double SPOT_PRICE = 100;
  private static final double IMPLIED_VOLATILITY = 0.5;
  private static final GreekToPositionGreekConverter G_TO_PG_CONVERTER = new GreekToPositionGreekConverter();
  private static final GreekToValueGreekConverter G_TO_VG_CONVERTER = new GreekToValueGreekConverter();
  private static final GreekResultCollection GREEKS;
  private static final GreekDataBundle GREEKS_DATA;
  private static final OptionTradeData TRADE_DATA = new OptionTradeData(N, PV);
  private static final double EPS = 1e-6;

  static {
    GREEKS = new GreekResultCollection();
    GREEKS.put(Greek.DELTA, DELTA);
    GREEKS.put(Greek.GAMMA, GAMMA);
    GREEKS.put(Greek.VANNA, VANNA);
    final Map<UnderlyingType, Double> map = new HashMap<>();
    map.put(UnderlyingType.SPOT_PRICE, SPOT_PRICE);
    map.put(UnderlyingType.IMPLIED_VOLATILITY, IMPLIED_VOLATILITY);
    GREEKS_DATA = new GreekDataBundle(GREEKS, map, TRADE_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    G_TO_PG_CONVERTER.evaluate((GreekDataBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    G_TO_VG_CONVERTER.evaluate((GreekDataBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGreekResultMissingDataValueGreek() {
    final GreekResultCollection greeks = new GreekResultCollection();
    greeks.put(Greek.DELTA, 5.);
    final Map<UnderlyingType, Double> data = Collections.<UnderlyingType, Double> singletonMap(UnderlyingType.BOND_YIELD, 0.04);
    G_TO_VG_CONVERTER.evaluate(new GreekDataBundle(greeks, data, TRADE_DATA));
  }

  @Test
  public void test() {
    final Map<PositionGreek, Double> positionGreeks = G_TO_PG_CONVERTER.evaluate(GREEKS_DATA);
    assertEquals(DELTA * N, positionGreeks.get(new PositionGreek(Greek.DELTA)), EPS);
    assertEquals(GAMMA * N, positionGreeks.get(new PositionGreek(Greek.GAMMA)), EPS);
    assertEquals(VANNA * N, positionGreeks.get(new PositionGreek(Greek.VANNA)), EPS);

    final Map<ValueGreek, Double> valueGreeks = G_TO_VG_CONVERTER.evaluate(GREEKS_DATA);
    assertEquals(DELTA * N * PV * SPOT_PRICE, valueGreeks.get(new ValueGreek(Greek.DELTA)), EPS);
    assertEquals(GAMMA * N * PV * SPOT_PRICE * SPOT_PRICE, valueGreeks.get(new ValueGreek(Greek.GAMMA)), EPS);
    assertEquals(VANNA * N * PV * SPOT_PRICE * IMPLIED_VOLATILITY, valueGreeks.get(new ValueGreek(Greek.VANNA)), EPS);

  }
}
