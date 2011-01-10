/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.trade.OptionTradeData;

/**
 * 
 */
public class GreekDataBundleTest {
  private static final double DELTA_VALUE = 120;
  private static final UnderlyingType UNDERLYING_TYPE = UnderlyingType.SPOT_PRICE;
  private static final double SPOT_VALUE = 10;
  private static final GreekResultCollection GREEK_RESULTS = new GreekResultCollection();
  private static final Map<UnderlyingType, Double> UNDERLYING_DATA = new HashMap<UnderlyingType, Double>();
  private static final OptionTradeData OPTION_TRADE_DATA = new OptionTradeData(100, 10);
  private static final GreekDataBundle DATA;

  static {
    GREEK_RESULTS.put(Greek.DELTA, DELTA_VALUE);
    UNDERLYING_DATA.put(UNDERLYING_TYPE, SPOT_VALUE);
    DATA = new GreekDataBundle(GREEK_RESULTS, UNDERLYING_DATA, OPTION_TRADE_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRiskFactors() {
    new GreekDataBundle(null, UNDERLYING_DATA, OPTION_TRADE_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullUnderlyingData() {
    new GreekDataBundle(GREEK_RESULTS, null, OPTION_TRADE_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyRiskFactors() {
    new GreekDataBundle(new GreekResultCollection(), UNDERLYING_DATA, OPTION_TRADE_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyUnderlyingData() {
    new GreekDataBundle(GREEK_RESULTS, Collections.<UnderlyingType, Double> emptyMap(), OPTION_TRADE_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTradeData() {
    new GreekDataBundle(GREEK_RESULTS, UNDERLYING_DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRiskFactorResults() {
    DATA.getGreekResultForGreek(Greek.GAMMA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUnderlyingData() {
    DATA.getUnderlyingDataForType(UnderlyingType.BOND_YIELD);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getGreekResults(), GREEK_RESULTS);
    assertEquals(DATA.getUnderlyingData(), UNDERLYING_DATA);
    assertEquals(DATA.getGreekResultForGreek(Greek.DELTA), DELTA_VALUE, 0);
    assertEquals(DATA.getUnderlyingDataForType(UNDERLYING_TYPE), SPOT_VALUE, 0);
    assertEquals(DATA.getOptionTradeData(), OPTION_TRADE_DATA);
  }

  @Test
  public void testEqualsAndHashCode() {
    GreekDataBundle other = new GreekDataBundle(GREEK_RESULTS, UNDERLYING_DATA, OPTION_TRADE_DATA);
    final Map<UnderlyingType, Double> underlyingData = new HashMap<UnderlyingType, Double>();
    underlyingData.put(UnderlyingType.COST_OF_CARRY, SPOT_VALUE);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    final GreekResultCollection greeks = new GreekResultCollection();
    greeks.put(Greek.DELTA, DELTA_VALUE + 1);
    other = new GreekDataBundle(greeks, UNDERLYING_DATA, OPTION_TRADE_DATA);
    assertFalse(DATA.equals(other));
    other = new GreekDataBundle(GREEK_RESULTS, underlyingData, OPTION_TRADE_DATA);
    assertFalse(DATA.equals(other));
    other = new GreekDataBundle(GREEK_RESULTS, UNDERLYING_DATA, new OptionTradeData(200, 10));
    assertFalse(DATA.equals(other));
  }
}
