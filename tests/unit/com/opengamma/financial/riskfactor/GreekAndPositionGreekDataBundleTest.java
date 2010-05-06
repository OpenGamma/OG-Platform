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
import com.opengamma.financial.pnl.Underlying;

/**
 *
 */
public class GreekAndPositionGreekDataBundleTest {
  private static final Map<Object, Double> D;
  private static final GreekResultCollection GREEKS = new GreekResultCollection();
  private static final Map<PositionGreek, RiskFactorResult<?>> RISK_FACTOR = new HashMap<PositionGreek, RiskFactorResult<?>>();
  private static final Map<Greek, Map<Object, Double>> UNDERLYING = new HashMap<Greek, Map<Object, Double>>();
  private static final GreekDataBundle GREEKS_DATA;
  private static final PositionGreekDataBundle POSITION_GREEKS_DATA;

  static {
    GREEKS.put(Greek.DELTA, new SingleGreekResult(0.12));
    GREEKS.put(Greek.GAMMA, new SingleGreekResult(0.34));
    RISK_FACTOR.put(new PositionGreek(Greek.DELTA), new SingleRiskFactorResult(12.));
    RISK_FACTOR.put(new PositionGreek(Greek.GAMMA), new SingleRiskFactorResult(34.));
    D = new HashMap<Object, Double>();
    D.put(TradeData.NUMBER_OF_CONTRACTS, 200.);
    D.put(Underlying.SPOT_PRICE, 40.);
    UNDERLYING.put(Greek.DELTA, D);
    UNDERLYING.put(Greek.GAMMA, D);
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
    new GreekDataBundle(GREEKS, new HashMap<Greek, Map<Object, Double>>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGreekValueForGreek() {
    GREEKS_DATA.getGreekValueForGreek(Greek.CARRY_RHO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingForGreek() {
    GREEKS_DATA.getAllUnderlyingDataForGreek(Greek.CARRY_RHO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingDataForGreek() {
    GREEKS_DATA.getUnderlyingDataForGreek(Greek.DELTA, Underlying.COST_OF_CARRY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRiskFactors() {
    new PositionGreekDataBundle(null, UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyRiskFactors() {
    new PositionGreekDataBundle(Collections.<PositionGreek, RiskFactorResult<?>> emptyMap(), UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData2() {
    new PositionGreekDataBundle(RISK_FACTOR, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyData2() {
    new PositionGreekDataBundle(RISK_FACTOR, new HashMap<Greek, Map<Object, Double>>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGreekValueForPositionGreek() {
    POSITION_GREEKS_DATA.getRiskFactorValueForRiskFactor(new PositionGreek(Greek.CARRY_RHO));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingForPositionGreek() {
    POSITION_GREEKS_DATA.getAllUnderlyingDataForRiskFactor(new PositionGreek(Greek.CARRY_RHO));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingDataForPositionGreek() {
    POSITION_GREEKS_DATA.getUnderlyingDataForRiskFactor(new PositionGreek(Greek.DELTA), Underlying.COST_OF_CARRY);
  }

  @Test
  public void test() {
    final PositionGreek delta = new PositionGreek(Greek.DELTA);
    assertEquals(GREEKS_DATA.getAllGreekValues(), GREEKS);
    assertEquals(GREEKS_DATA.getAllUnderlyingData(), UNDERLYING);
    assertEquals(GREEKS_DATA.getAllUnderlyingDataForGreek(Greek.DELTA), UNDERLYING.get(Greek.DELTA));
    assertEquals(GREEKS_DATA.getUnderlyingDataForGreek(Greek.DELTA, TradeData.NUMBER_OF_CONTRACTS), D
        .get(TradeData.NUMBER_OF_CONTRACTS));
    assertEquals(GREEKS_DATA.getUnderlyingDataForGreek(Greek.DELTA, Underlying.SPOT_PRICE), D
        .get(Underlying.SPOT_PRICE));
    assertEquals(POSITION_GREEKS_DATA.getAllRiskFactorValues(), RISK_FACTOR);
    assertEquals(POSITION_GREEKS_DATA.getAllUnderlyingData(), UNDERLYING);
    assertEquals(POSITION_GREEKS_DATA.getAllUnderlyingDataForRiskFactor(delta), UNDERLYING.get(Greek.DELTA));
    assertEquals(POSITION_GREEKS_DATA.getUnderlyingDataForRiskFactor(delta, TradeData.NUMBER_OF_CONTRACTS), D
        .get(TradeData.NUMBER_OF_CONTRACTS));
    assertEquals(POSITION_GREEKS_DATA.getUnderlyingDataForRiskFactor(delta, Underlying.SPOT_PRICE), D
        .get(Underlying.SPOT_PRICE));
  }
}
