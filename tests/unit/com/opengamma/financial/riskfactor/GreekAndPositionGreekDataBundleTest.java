/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.sensitivity.PositionGreek;
import com.opengamma.financial.sensitivity.PositionGreekResult;
import com.opengamma.financial.sensitivity.SinglePositionGreekResult;

/**
 *
 */
public class GreekAndPositionGreekDataBundleTest {
  private static final GreekResultCollection GREEKS = new GreekResultCollection();
  private static final Map<PositionGreek, PositionGreekResult<?>> RISK_FACTOR = new HashMap<PositionGreek, PositionGreekResult<?>>();
  private static final Map<Object, Double> UNDERLYING = new HashMap<Object, Double>();
  private static final GreekDataBundle GREEKS_DATA;
  private static final PositionGreekDataBundle POSITION_GREEKS_DATA;

  static {
    GREEKS.put(Greek.DELTA, new SingleGreekResult(0.12));
    GREEKS.put(Greek.GAMMA, new SingleGreekResult(0.34));
    RISK_FACTOR.put(new PositionGreek(Greek.DELTA), new SinglePositionGreekResult(12.));
    RISK_FACTOR.put(new PositionGreek(Greek.GAMMA), new SinglePositionGreekResult(34.));
    UNDERLYING.put(TradeData.NUMBER_OF_CONTRACTS, 200.);
    UNDERLYING.put(UnderlyingType.SPOT_PRICE, 40.);
    GREEKS_DATA = new GreekDataBundle(GREEKS, UNDERLYING);
    POSITION_GREEKS_DATA = new PositionGreekDataBundle(RISK_FACTOR, UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullGreeks() {
    new GreekDataBundle(null, UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyGreeks() {
    new GreekDataBundle(new GreekResultCollection(), UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData1() {
    new GreekDataBundle(GREEKS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyData1() {
    new GreekDataBundle(GREEKS, new HashMap<Object, Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGreekValueForGreek() {
    GREEKS_DATA.getGreekResultForGreek(Greek.CARRY_RHO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRiskFactors() {
    new PositionGreekDataBundle(null, UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyRiskFactors() {
    new PositionGreekDataBundle(Collections.<PositionGreek, PositionGreekResult<?>> emptyMap(), UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData2() {
    new PositionGreekDataBundle(RISK_FACTOR, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyData2() {
    new PositionGreekDataBundle(RISK_FACTOR, new HashMap<Object, Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGreekValueForPositionGreek() {
    POSITION_GREEKS_DATA.getRiskFactorValueForRiskFactor(new PositionGreek(Greek.CARRY_RHO));
  }

  @Test
  public void test() {
    assertEquals(GREEKS_DATA.getGreekResults(), GREEKS);
    assertEquals(GREEKS_DATA.getUnderlyingData(), UNDERLYING);
    assertEquals(POSITION_GREEKS_DATA.getRiskFactorResults(), RISK_FACTOR);
    assertEquals(POSITION_GREEKS_DATA.getUnderlyingData(), UNDERLYING);
  }
}
