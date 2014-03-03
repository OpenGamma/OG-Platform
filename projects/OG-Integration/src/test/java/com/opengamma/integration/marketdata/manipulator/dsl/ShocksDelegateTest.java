/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ShocksDelegateTest {

  private static final String FOO = "foo";
  private static final String BAR = "bar";

  @Test
  public void list() {
    ShocksDelegate delegate = new ShocksDelegate();
    delegate.setProperty(FOO, ImmutableList.of(1, 2, 3));
    delegate.setProperty(BAR, ImmutableList.of("a", "b", "c"));
    List<Map<String,Object>> params = delegate.list();
    assertEquals(3, params.size());

    assertEquals(1, params.get(0).get(FOO));
    assertEquals("a", params.get(0).get(BAR));

    assertEquals(2, params.get(1).get(FOO));
    assertEquals("b", params.get(1).get(BAR));

    assertEquals(3, params.get(2).get(FOO));
    assertEquals("c", params.get(2).get(BAR));
  }

  @Test
  public void listKeyOrder() {
    ShocksDelegate delegate1 = new ShocksDelegate();
    delegate1.setProperty(FOO, ImmutableList.of(1, 2, 3));
    delegate1.setProperty(BAR, ImmutableList.of("a", "b", "c"));
    List<Map<String,Object>> params1 = delegate1.list();
    assertEquals(3, params1.size());
    Iterator<Map.Entry<String, Object>> itr1 = params1.get(0).entrySet().iterator();
    assertEquals(FOO, itr1.next().getKey());
    assertEquals(BAR, itr1.next().getKey());

    ShocksDelegate delegate2 = new ShocksDelegate();
    delegate2.setProperty(BAR, ImmutableList.of("a", "b", "c"));
    delegate2.setProperty(FOO, ImmutableList.of(1, 2, 3));
    List<Map<String,Object>> params2 = delegate2.list();
    assertEquals(3, params2.size());
    Iterator<Map.Entry<String, Object>> itr2 = params2.get(0).entrySet().iterator();
    assertEquals(BAR, itr2.next().getKey());
    assertEquals(FOO, itr2.next().getKey());
  }

  @Test
  public void cartesianProduct() {
    ShocksDelegate delegate = new ShocksDelegate();
    delegate.setProperty(FOO, ImmutableList.of(1, 2));
    delegate.setProperty(BAR, ImmutableList.of("a", "b"));
    List<Map<String,Object>> params = delegate.cartesianProduct();
    assertEquals(4, params.size());

    assertEquals(1, params.get(0).get(FOO));
    assertEquals("a", params.get(0).get(BAR));

    assertEquals(1, params.get(1).get(FOO));
    assertEquals("b", params.get(1).get(BAR));

    assertEquals(2, params.get(2).get(FOO));
    assertEquals("a", params.get(2).get(BAR));

    assertEquals(2, params.get(3).get(FOO));
    assertEquals("b", params.get(3).get(BAR));
  }

  @Test
  public void cartesianProductKeyOrder() {
    ShocksDelegate delegate1 = new ShocksDelegate();
    delegate1.setProperty(FOO, ImmutableList.of(1, 2, 3));
    delegate1.setProperty(BAR, ImmutableList.of("a", "b", "c"));
    List<Map<String,Object>> params1 = delegate1.cartesianProduct();
    assertEquals(9, params1.size());
    Iterator<Map.Entry<String, Object>> itr1 = params1.get(0).entrySet().iterator();
    assertEquals(FOO, itr1.next().getKey());
    assertEquals(BAR, itr1.next().getKey());

    ShocksDelegate delegate2 = new ShocksDelegate();
    delegate2.setProperty(BAR, ImmutableList.of("a", "b", "c"));
    delegate2.setProperty(FOO, ImmutableList.of(1, 2, 3));
    List<Map<String,Object>> params2 = delegate2.cartesianProduct();
    assertEquals(9, params2.size());
    Iterator<Map.Entry<String, Object>> itr2 = params2.get(0).entrySet().iterator();
    assertEquals(BAR, itr2.next().getKey());
    assertEquals(FOO, itr2.next().getKey());
  }
}
