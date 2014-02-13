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
    ShocksDelegate delegate = new ShocksDelegate();
    delegate.setProperty(FOO, ImmutableList.of(1, 2, 3));
    delegate.setProperty(BAR, ImmutableList.of("a", "b", "c"));
    List<Map<String,Object>> params = delegate.list();
    assertEquals(3, params.size());
    Iterator<Map.Entry<String, Object>> itr = params.get(0).entrySet().iterator();
    assertEquals(BAR, itr.next().getKey());
    assertEquals(FOO, itr.next().getKey());
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
    ShocksDelegate delegate = new ShocksDelegate();
    delegate.setProperty(FOO, ImmutableList.of(1, 2, 3));
    delegate.setProperty(BAR, ImmutableList.of("a", "b", "c"));
    List<Map<String,Object>> params = delegate.cartesianProduct();
    assertEquals(9, params.size());
    Iterator<Map.Entry<String, Object>> itr = params.get(0).entrySet().iterator();
    assertEquals(BAR, itr.next().getKey());
    assertEquals(FOO, itr.next().getKey());
  }
}
