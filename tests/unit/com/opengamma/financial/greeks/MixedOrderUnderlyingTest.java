/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.financial.pnl.UnderlyingType;

public class MixedOrderUnderlyingTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNullMap() {
    new MixedOrderUnderlying((Map<Integer, UnderlyingType>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSmallMap() {
    new MixedOrderUnderlying(Collections.singletonMap(1, UnderlyingType.SPOT_PRICE));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadOrder1() {
    final Map<Integer, UnderlyingType> map = new HashMap<Integer, UnderlyingType>();
    map.put(0, UnderlyingType.BOND_YIELD);
    map.put(1, UnderlyingType.COST_OF_CARRY);
    new MixedOrderUnderlying(map);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadOrder2() {
    final Set<NthOrderUnderlying> orders = new HashSet<NthOrderUnderlying>();
    orders.add(new NthOrderUnderlying(0, UnderlyingType.SPOT_PRICE));
    orders.add(new NthOrderUnderlying(1, UnderlyingType.BOND_YIELD));
    new MixedOrderUnderlying(orders);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSet() {
    new MixedOrderUnderlying((Set<NthOrderUnderlying>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSmallSet() {
    new MixedOrderUnderlying(Collections.singleton(new NthOrderUnderlying(2, UnderlyingType.BOND_YIELD)));
  }

  @Test
  public void test() {
    final int order1 = 2;
    final int order2 = 3;
    final UnderlyingType type1 = UnderlyingType.SPOT_PRICE;
    final UnderlyingType type2 = UnderlyingType.SPOT_VOLATILITY;
    final Map<Integer, UnderlyingType> map1 = new HashMap<Integer, UnderlyingType>();
    map1.put(order1, type1);
    map1.put(order2, type2);
    final Set<NthOrderUnderlying> set = new HashSet<NthOrderUnderlying>();
    set.add(new NthOrderUnderlying(order1, type1));
    set.add(new NthOrderUnderlying(order2, type2));
    final Map<Integer, UnderlyingType> map2 = new HashMap<Integer, UnderlyingType>();
    map2.put(order1, type1);
    map2.put(order2, type2);
    final MixedOrderUnderlying underlying = new MixedOrderUnderlying(map1);
    assertFalse(underlying.equals(new MixedOrderUnderlying(map2)));
    assertFalse(underlying.equals(new MixedOrderUnderlying(set)));
    assertEquals(underlying.getOrder(), order1 + order2);
    final Set<UnderlyingType> types = underlying.getUnderlyings();
    assertEquals(types.size(), 2);
    assertTrue(types.contains(type1));
    assertTrue(types.contains(type2));
    assertFalse(underlying.equals(Sets.newHashSet(new NthOrderUnderlying(order2, type1), new NthOrderUnderlying(order2, type2))));
    assertFalse(underlying.equals(Sets.newHashSet(new NthOrderUnderlying(order1, type1), new NthOrderUnderlying(order2, type1))));
  }
}
