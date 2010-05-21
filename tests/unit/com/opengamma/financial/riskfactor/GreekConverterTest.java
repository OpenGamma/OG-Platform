/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.sensitivity.PositionGreek;
import com.opengamma.financial.sensitivity.ValueGreek;

/**
 *
 */
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
  private static final PositionGreekToValueGreekConverter PG_TO_VG_CONVERTER = new PositionGreekToValueGreekConverter();
  private static final GreekResultCollection GREEKS;
  private static final GreekDataBundle GREEKS_DATA;
  private static final double EPS = 1e-6;

  static {
    GREEKS = new GreekResultCollection();
    GREEKS.put(Greek.DELTA, DELTA);
    GREEKS.put(Greek.GAMMA, GAMMA);
    GREEKS.put(Greek.VANNA, VANNA);
    final Map<Object, Double> map = new HashMap<Object, Double>();
    map.put(UnderlyingType.SPOT_PRICE, SPOT_PRICE);
    map.put(TradeData.NUMBER_OF_CONTRACTS, N);
    map.put(TradeData.POINT_VALUE, PV);
    map.put(UnderlyingType.IMPLIED_VOLATILITY, IMPLIED_VOLATILITY);
    GREEKS_DATA = new GreekDataBundle(GREEKS, map);
  }

  @Test(expected = NullPointerException.class)
  public void testNull1() {
    G_TO_PG_CONVERTER.evaluate((GreekDataBundle) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull2() {
    G_TO_VG_CONVERTER.evaluate((GreekDataBundle) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull3() {
    PG_TO_VG_CONVERTER.evaluate((PositionGreekDataBundle) null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testGreekResultMissingDataValueGreek() {
    final GreekResultCollection greeks = new GreekResultCollection();
    greeks.put(Greek.DELTA, 5.);
    final Map<Object, Double> data = Collections.<Object, Double> singletonMap(TradeData.NUMBER_OF_CONTRACTS, N);
    G_TO_VG_CONVERTER.evaluate(new GreekDataBundle(greeks, data));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRiskFactorResultType() {
    final Map<PositionGreek, Double> riskFactors = Collections.<PositionGreek, Double> singletonMap(new PositionGreek(Greek.DELTA), 5.);
    final Map<Object, Double> data = Collections.<Object, Double> singletonMap(TradeData.NUMBER_OF_CONTRACTS, N);
    PG_TO_VG_CONVERTER.evaluate(new PositionGreekDataBundle(riskFactors, data));
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
    
    final Map<ValueGreek, Double> valueGreeksFromPositionGreeks = PG_TO_VG_CONVERTER.evaluate(new PositionGreekDataBundle(positionGreeks, GREEKS_DATA.getUnderlyingData()));
    for (final Map.Entry<ValueGreek, Double> entry : valueGreeksFromPositionGreeks.entrySet()) {
      assertTrue(valueGreeks.containsKey(entry.getKey()));
      assertEquals(valueGreeks.get(entry.getKey()), entry.getValue(), EPS);
    }
  }
}
