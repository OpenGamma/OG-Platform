/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MapUtilsTest {

  @SuppressWarnings("unchecked")
  public void test_constructor() throws Exception {
    Constructor<?>[] cons = MapUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    Constructor<MapUtils> con = (Constructor<MapUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  public void test_putIfAbsentGet() {
    Map<String, Map<Integer, String>> map = Maps.newHashMap();
    assertEquals(0, map.size());
    
    Map<Integer, String> inner1 = MapUtils.putIfAbsentGet(map, "A", new HashMap<Integer, String>());
    assertEquals(1, map.size());
    assertEquals(0, inner1.size());
    assertSame(inner1, map.get("A"));
    
    Map<Integer, String> inner2 = MapUtils.putIfAbsentGet(map, "A", new HashMap<Integer, String>());
    assertEquals(1, map.size());
    assertEquals(0, inner2.size());
    assertSame(inner2, inner1);
    
    String value1 = MapUtils.putIfAbsentGet(inner1, 6, "X");
    assertEquals(1, map.size());
    assertEquals(1, inner1.size());
    assertEquals("X", value1);
    
    String value2 = MapUtils.putIfAbsentGet(inner1, 6, "Y");
    assertEquals(1, map.size());
    assertEquals(1, inner1.size());
    assertEquals("X", value2);
  }

}
