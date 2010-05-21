/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MultipleGreekResultTest {

  @Test(expected = NullPointerException.class)
  public void testNull() {
    new MultipleGreekResult(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    new MultipleGreekResult(Collections.<String, Double> emptyMap());
  }

  @Test
  public void test() {
    final Map<String, Double> map = new HashMap<String, Double>();
    map.put("A", 1.);
    map.put("B", 2.);
    final MultipleGreekResult result = new MultipleGreekResult(map);
    assertTrue(result.isMultiValued());
    assertEquals(map, result.getResult());
  }

  @Test
  public void testHashCode() {
    final Map<String, Double> map1 = new HashMap<String, Double>();
    map1.put("A", 1.);
    map1.put("B", 2.);
    final MultipleGreekResult result1 = new MultipleGreekResult(map1);
    MultipleGreekResult result2 = new MultipleGreekResult(map1);
    assertTrue(result1.hashCode() == result2.hashCode());

    Map<String, Double> map2 = new HashMap<String, Double>();
    map2.put("A", 2.);
    map2.put("B", 2.);
    result2 = new MultipleGreekResult(map2);
    assertFalse(result1.hashCode() == result2.hashCode());

    map2 = new HashMap<String, Double>();
    map2.put("A", 1.);
    map2.put("C", 2.);
    result2 = new MultipleGreekResult(map2);
    assertFalse(result1.hashCode() == result2.hashCode());
  }

  @Test
  public void testEquals() {
    final Map<String, Double> map1 = new HashMap<String, Double>();
    map1.put("A", 1.);
    map1.put("B", 2.);
    final MultipleGreekResult result1 = new MultipleGreekResult(map1);
    assertTrue(result1.equals(result1));
    assertFalse(result1.equals(null));
    assertFalse(result1.equals("foo"));

    MultipleGreekResult result2 = new MultipleGreekResult(map1);
    assertTrue(result1.equals(result2));

    Map<String, Double> map2 = new HashMap<String, Double>();
    map2.put("A", 2.);
    map2.put("B", 2.);
    result2 = new MultipleGreekResult(map2);
    assertFalse(result1.equals(result2));

    map2 = new HashMap<String, Double>();
    map2.put("A", 1.);
    map2.put("C", 2.);
    result2 = new MultipleGreekResult(map2);
    assertFalse(result1.equals(result2));
  }

  @Test
  public void testToString() {
    final Map<String, Double> map1 = new HashMap<String, Double>();
    map1.put("Apple", 1.);
    map1.put("Banana", 2.);
    final MultipleGreekResult result1 = new MultipleGreekResult(map1);
    final String toString = result1.toString();
    assertNotNull(toString);
    assertTrue(toString.indexOf("MultipleGreekResult") > -1);
    assertTrue(toString.indexOf("Apple") > -1);
    assertTrue(toString.indexOf("Banana") > -1);
  }

}
