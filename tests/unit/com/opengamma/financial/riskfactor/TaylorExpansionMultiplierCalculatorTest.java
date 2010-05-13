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
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.financial.greeks.MixedOrderUnderlying;
import com.opengamma.financial.greeks.NthOrderUnderlying;
import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.UnderlyingType;

public class TaylorExpansionMultiplierCalculatorTest {
  private static final Underlying NEW_TYPE = new Underlying() {

    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    public Set<UnderlyingType> getUnderlyings() {
      return null;
    }

  };
  private static final Map<Object, Double> UNDERLYING_DATA = new HashMap<Object, Double>();
  private static final double X = 120;
  private static final double Y = 0.5;
  private static final Underlying ZEROTH_ORDER = new NthOrderUnderlying(0, null);
  private static final Underlying FIRST_ORDER = new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE);
  private static final Underlying SECOND_ORDER = new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE);
  private static final Underlying THIRD_ORDER = new NthOrderUnderlying(3, UnderlyingType.SPOT_PRICE);
  private static final Underlying FOURTH_ORDER = new NthOrderUnderlying(4, UnderlyingType.SPOT_PRICE);
  private static final Underlying FIFTH_ORDER = new NthOrderUnderlying(5, UnderlyingType.SPOT_PRICE);
  private static final Underlying MIXED_ORDER;
  private static final double EPS = 1e-12;

  static {
    UNDERLYING_DATA.put(UnderlyingType.SPOT_PRICE, X);
    UNDERLYING_DATA.put(UnderlyingType.IMPLIED_VOLATILITY, Y);
    MIXED_ORDER = new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(4, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(5, UnderlyingType.IMPLIED_VOLATILITY)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullOrder1() {
    TaylorExpansionMultiplierCalculator.getMultiplier(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingType1() {
    TaylorExpansionMultiplierCalculator.getMultiplier(NEW_TYPE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullOrder2() {
    TaylorExpansionMultiplierCalculator.getMultiplier(UNDERLYING_DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMap() {
    TaylorExpansionMultiplierCalculator.getMultiplier(null, FIRST_ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyMap() {
    TaylorExpansionMultiplierCalculator.getMultiplier(Collections.<Object, Double> emptyMap(), FIRST_ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingType2() {
    TaylorExpansionMultiplierCalculator.getMultiplier(NEW_TYPE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    TaylorExpansionMultiplierCalculator.getMultiplier(Collections.<Object, Double> singletonMap(UnderlyingType.SPOT_PRICE, null), FIFTH_ORDER);
  }

  @Test
  public void test() {
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(ZEROTH_ORDER), 1, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(FIRST_ORDER), 1, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(SECOND_ORDER), 1. / 2, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(THIRD_ORDER), 1. / 6, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(FOURTH_ORDER), 1. / 24, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(FIFTH_ORDER), 1. / 120, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(MIXED_ORDER), 1. / 2880, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(UNDERLYING_DATA, ZEROTH_ORDER), 1, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(UNDERLYING_DATA, FIRST_ORDER), X, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(UNDERLYING_DATA, SECOND_ORDER), X * X / 2, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(UNDERLYING_DATA, THIRD_ORDER), X * X * X / 6, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(UNDERLYING_DATA, FOURTH_ORDER), X * X * X * X / 24, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(UNDERLYING_DATA, FIFTH_ORDER), X * X * X * X * X / 120, EPS);
    assertEquals(TaylorExpansionMultiplierCalculator.getMultiplier(UNDERLYING_DATA, MIXED_ORDER), X * X * X * X * Y * Y * Y * Y * Y / 2880, EPS);
  }
}
