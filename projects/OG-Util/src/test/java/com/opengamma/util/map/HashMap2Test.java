/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link HashMap2} implementation.
 */
@Test(groups = TestGroup.UNIT)
public class HashMap2Test {

  private void testBasicOperations(final Map2<String, String, String> map) {
    assertTrue(map.isEmpty());
    assertEquals(map.size(), 0);
    assertEquals(map.put("B", "A", "Bar"), null);
    assertEquals(map.put("A", "B", "Foo"), null);
    assertFalse(map.isEmpty());
    assertEquals(map.size(), 2);
    assertEquals(map.get("A", "B"), "Foo");
    assertEquals(map.get("B", "A"), "Bar");
    assertEquals(map.get("X", "Y"), null);
    assertTrue(map.containsKey("A", "B"));
    assertFalse(map.containsKey("X", "Y"));
    map.clear();
    assertTrue(map.isEmpty());
    assertEquals(map.size(), 0);
    assertFalse(map.containsKey("A", "B"));
  }

  public void testBasicOperations_strongKeys() {
    testBasicOperations(new HashMap2<String, String, String>(HashMap2.STRONG_KEYS));
  }

  public void testBasicOperations_weakKeys() {
    testBasicOperations(new HashMap2<String, String, String>(HashMap2.WEAK_KEYS));
  }

  private void testRemove(final Map2<String, String, String> map) {
    map.put("A", "B", "Foo");
    assertEquals(map.size(), 1);
    assertEquals(map.remove("A", "B"), "Foo");
    assertEquals(map.size(), 0);
    assertEquals(map.remove("A", "B"), null);
  }

  public void testRemove_strongKeys() {
    testRemove(new HashMap2<String, String, String>(HashMap2.STRONG_KEYS));
  }

  public void testRemove_weakKeys() {
    testRemove(new HashMap2<String, String, String>(HashMap2.WEAK_KEYS));
  }

  private void testPutIfAbsent(final Map2<String, String, String> map) {
    assertEquals(map.put("A", "B", "Foo"), null);
    assertEquals(map.put("B", "A", "Bar"), null);
    assertEquals(map.put("A", "B", "Cow"), "Foo");
    assertEquals(map.put("B", "A", "Dog"), "Bar");
    assertEquals(map.putIfAbsent("A", "B", "Foo"), "Cow");
    assertEquals(map.putIfAbsent("B", "A", "Bar"), "Dog");
    assertEquals(map.get("A", "B"), "Cow");
    assertEquals(map.get("B", "A"), "Dog");
  }

  public void testPutIfAbsent_strongKeys() {
    testPutIfAbsent(new HashMap2<String, String, String>(HashMap2.STRONG_KEYS));
  }

  public void testPutIfAbsent_weakKeys() {
    testPutIfAbsent(new HashMap2<String, String, String>(HashMap2.WEAK_KEYS));
  }

}
