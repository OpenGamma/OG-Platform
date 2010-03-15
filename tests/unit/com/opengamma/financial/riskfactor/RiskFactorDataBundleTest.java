/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.pnl.OptionTradeData;
import com.opengamma.financial.pnl.Underlying;

/**
 * @author emcleod
 *
 */
public class RiskFactorDataBundleTest {
  private static final Map<Object, Double> D;
  private static final GreekResultCollection GREEKS = new GreekResultCollection();
  private static final Map<Greek, Map<Object, Double>> UNDERLYING = new HashMap<Greek, Map<Object, Double>>();
  private static final RiskFactorDataBundle DATA;

  static {
    GREEKS.put(Greek.DELTA, new SingleGreekResult(0.12));
    GREEKS.put(Greek.GAMMA, new SingleGreekResult(0.34));
    D = new HashMap<Object, Double>();
    D.put(OptionTradeData.NUMBER_OF_CONTRACTS, 200.);
    D.put(Underlying.SPOT_PRICE, 40.);
    UNDERLYING.put(Greek.DELTA, D);
    UNDERLYING.put(Greek.GAMMA, D);
    DATA = new RiskFactorDataBundle(GREEKS, UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullGreeks() {
    new RiskFactorDataBundle(null, UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyGreeks() {
    new RiskFactorDataBundle(new GreekResultCollection(), UNDERLYING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new RiskFactorDataBundle(GREEKS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyData() {
    new RiskFactorDataBundle(GREEKS, new HashMap<Greek, Map<Object, Double>>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGreekValueForGreek() {
    DATA.getGreekValueForGreek(Greek.CARRY_RHO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingForGreek() {
    DATA.getAllUnderlyingDataForGreek(Greek.CARRY_RHO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingDataForGreek() {
    DATA.getUnderlyingDataForGreek(Greek.DELTA, Underlying.COST_OF_CARRY);
  }

  @Test
  public void test() {
    assertEquals(DATA.getAllGreekValues(), GREEKS);
    assertEquals(DATA.getAllUnderlyingData(), UNDERLYING);
    assertEquals(DATA.getAllUnderlyingDataForGreek(Greek.DELTA), UNDERLYING.get(Greek.DELTA));
    assertEquals(DATA.getUnderlyingDataForGreek(Greek.DELTA, OptionTradeData.NUMBER_OF_CONTRACTS), D.get(OptionTradeData.NUMBER_OF_CONTRACTS));
    assertEquals(DATA.getUnderlyingDataForGreek(Greek.DELTA, Underlying.SPOT_PRICE), D.get(Underlying.SPOT_PRICE));
  }
}
