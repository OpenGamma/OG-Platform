/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MixedOrderUnderlyingTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap() {
    new MixedOrderUnderlying((TreeMap<Integer, UnderlyingType>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSmallMap() {
    final TreeMap<Integer, UnderlyingType> map = new TreeMap<>();
    map.put(1, UnderlyingType.SPOT_PRICE);
    new MixedOrderUnderlying(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadOrder1() {
    final TreeMap<Integer, UnderlyingType> map = new TreeMap<>();
    map.put(0, UnderlyingType.BOND_YIELD);
    map.put(1, UnderlyingType.COST_OF_CARRY);
    new MixedOrderUnderlying(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadOrder2() {
    final List<NthOrderUnderlying> orders = new ArrayList<>();
    orders.add(new NthOrderUnderlying(0, UnderlyingType.SPOT_PRICE));
    orders.add(new NthOrderUnderlying(1, UnderlyingType.BOND_YIELD));
    new MixedOrderUnderlying(orders);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSet() {
    new MixedOrderUnderlying((List<NthOrderUnderlying>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSmallSet() {
    new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(2, UnderlyingType.BOND_YIELD)));
  }

  @Test
  public void test() {
    final int order1 = 2;
    final int order2 = 3;
    final UnderlyingType type1 = UnderlyingType.SPOT_PRICE;
    final UnderlyingType type2 = UnderlyingType.SPOT_VOLATILITY;
    final TreeMap<Integer, UnderlyingType> map1 = new TreeMap<>();
    map1.put(order1, type1);
    map1.put(order2, type2);
    final List<NthOrderUnderlying> set = new ArrayList<>();
    set.add(new NthOrderUnderlying(order1, type1));
    set.add(new NthOrderUnderlying(order2, type2));
    final TreeMap<Integer, UnderlyingType> map2 = new TreeMap<>();
    map2.put(order1, type1);
    map2.put(order2, type2);
    final MixedOrderUnderlying underlying = new MixedOrderUnderlying(map1);
    assertFalse(underlying.equals(new MixedOrderUnderlying(map2)));
    assertFalse(underlying.equals(new MixedOrderUnderlying(set)));
    assertFalse(underlying.equals(new MixedOrderUnderlying(map1)));
    assertEquals(underlying.getOrder(), order1 + order2);
    final List<UnderlyingType> types = underlying.getUnderlyings();
    assertEquals(types.size(), 2);
    assertTrue(types.contains(type1));
    assertTrue(types.contains(type2));
    assertFalse(underlying.equals(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(order2, type1), new NthOrderUnderlying(order2, type2)))));
    assertFalse(underlying.equals(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(order1, type1), new NthOrderUnderlying(order2, type1)))));
  }
}
