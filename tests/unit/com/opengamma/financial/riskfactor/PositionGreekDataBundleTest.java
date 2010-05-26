/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.NthOrderUnderlying;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.sensitivity.PositionGreek;

/**
 * 
 */
public class PositionGreekDataBundleTest {
  private static final PositionGreek DELTA = new PositionGreek(Greek.DELTA);
  private static final double DELTA_VALUE = 120;
  private static final Object UNDERLYING = new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE);
  private static final double SPOT_VALUE = 10;
  private static final Map<PositionGreek, Double> RISK_FACTORS = new HashMap<PositionGreek, Double>();
  private static final Map<Object, Double> UNDERLYING_DATA = new HashMap<Object, Double>();
  private static final PositionGreekDataBundle DATA;

  static {
    RISK_FACTORS.put(DELTA, DELTA_VALUE);
    UNDERLYING_DATA.put(UNDERLYING, SPOT_VALUE);
    DATA = new PositionGreekDataBundle(RISK_FACTORS, UNDERLYING_DATA);
  }

  @Test(expected = NullPointerException.class)
  public void testNullRiskFactors() {
    new PositionGreekDataBundle(null, UNDERLYING_DATA);
  }

  @Test(expected = NullPointerException.class)
  public void testNullUnderlyingData() {
    new PositionGreekDataBundle(RISK_FACTORS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyRiskFactors() {
    new PositionGreekDataBundle(Collections.<PositionGreek, Double> emptyMap(), UNDERLYING_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyUnderlyingData() {
    new PositionGreekDataBundle(RISK_FACTORS, Collections.<Object, Double> emptyMap());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRiskFactorResults() {
    DATA.getRiskFactorValueForRiskFactor(new PositionGreek(Greek.GAMMA));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUnderlyingData() {
    DATA.getUnderlyingDataForObject(DELTA);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getRiskFactorResults(), RISK_FACTORS);
    assertEquals(DATA.getUnderlyingData(), UNDERLYING_DATA);
    assertEquals(DATA.getRiskFactorValueForRiskFactor(DELTA), DELTA_VALUE, 0);
    assertEquals(DATA.getUnderlyingDataForObject(UNDERLYING), SPOT_VALUE, 0);

  }

  @Test
  public void testEqualsAndHashCode() {
    final PositionGreekDataBundle data1 = new PositionGreekDataBundle(RISK_FACTORS, UNDERLYING_DATA);
    final Map<Object, Double> underlyingData = new HashMap<Object, Double>();
    underlyingData.putAll(UNDERLYING_DATA);
    underlyingData.put(TradeData.NUMBER_OF_CONTRACTS, 100.);
    final PositionGreekDataBundle data2 = new PositionGreekDataBundle(RISK_FACTORS, underlyingData);
    assertEquals(DATA, data1);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertFalse(DATA.equals(data2));
    assertFalse(DATA.hashCode() == data2.hashCode());
    assertEquals(DATA, DATA);
    assertFalse(DATA.equals(null));
    assertFalse(DATA.equals(RISK_FACTORS));
  }
}
