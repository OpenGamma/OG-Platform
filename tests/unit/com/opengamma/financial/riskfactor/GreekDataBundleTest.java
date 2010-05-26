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
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.NthOrderUnderlying;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.pnl.UnderlyingType;

/**
 * 
 */
public class GreekDataBundleTest {
  private static final double DELTA_VALUE = 120;
  private static final Object UNDERLYING = new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE);
  private static final double SPOT_VALUE = 10;
  private static final GreekResultCollection GREEK_RESULTS = new GreekResultCollection();
  private static final Map<Object, Double> UNDERLYING_DATA = new HashMap<Object, Double>();
  private static final GreekDataBundle DATA;

  static {
    GREEK_RESULTS.put(Greek.DELTA, DELTA_VALUE);
    UNDERLYING_DATA.put(UNDERLYING, SPOT_VALUE);
    DATA = new GreekDataBundle(GREEK_RESULTS, UNDERLYING_DATA);
  }

  @Test(expected = NullPointerException.class)
  public void testNullRiskFactors() {
    new GreekDataBundle(null, UNDERLYING_DATA);
  }

  @Test(expected = NullPointerException.class)
  public void testNullUnderlyingData() {
    new GreekDataBundle(GREEK_RESULTS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyRiskFactors() {
    new GreekDataBundle(new GreekResultCollection(), UNDERLYING_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyUnderlyingData() {
    new GreekDataBundle(GREEK_RESULTS, Collections.<Object, Double> emptyMap());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRiskFactorResults() {
    DATA.getGreekResultForGreek(Greek.GAMMA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUnderlyingData() {
    DATA.getUnderlyingDataForObject(Greek.DELTA);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getGreekResults(), GREEK_RESULTS);
    assertEquals(DATA.getUnderlyingData(), UNDERLYING_DATA);
    assertEquals(DATA.getGreekResultForGreek(Greek.DELTA), DELTA_VALUE, 0);
    assertEquals(DATA.getUnderlyingDataForObject(UNDERLYING), SPOT_VALUE, 0);

  }

  @Test
  public void testEqualsAndHashCode() {
    final GreekDataBundle data1 = new GreekDataBundle(GREEK_RESULTS, UNDERLYING_DATA);
    final Map<Object, Double> underlyingData = new HashMap<Object, Double>();
    underlyingData.putAll(UNDERLYING_DATA);
    underlyingData.put(TradeData.NUMBER_OF_CONTRACTS, 100.);
    final GreekDataBundle data2 = new GreekDataBundle(GREEK_RESULTS, underlyingData);
    assertEquals(DATA, data1);
    assertEquals(DATA.hashCode(), data1.hashCode());
    assertFalse(DATA.equals(data2));
    assertFalse(DATA.hashCode() == data2.hashCode());
    assertEquals(DATA, DATA);
    assertFalse(DATA.equals(null));
    assertFalse(DATA.equals(GREEK_RESULTS));
  }
}
