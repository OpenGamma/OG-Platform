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
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Tests the {@link HashMap2} implementation.
 */
@Test(groups = TestGroup.UNIT)
public class HashMap2Test {

  private void testBasicOperations(final Map2<String, String, String> map) {
    assertTrue(map.isEmpty());
    assertEquals(map.size(), 0);
    assertEquals(map.put(ObjectsPair.of("A", "B"), "Foo"), null);
    assertEquals(map.put("B", "A", "Bar"), null);
    assertFalse(map.isEmpty());
    assertEquals(map.size(), 2);
    assertEquals(map.get(ObjectsPair.of("A", "B")), "Foo");
    assertEquals(map.get(ObjectsPair.of("B", "A")), "Bar");
    assertEquals(map.get("A", "B"), "Foo");
    assertEquals(map.get("B", "A"), "Bar");
    assertEquals(map.get("X", "Y"), null);
    assertEquals(map.get(ObjectsPair.of("X", "Y")), null);
    assertTrue(map.containsKey(ObjectsPair.of("B", "A")));
    assertTrue(map.containsKey("A", "B"));
    assertFalse(map.containsKey("X", "Y"));
    assertFalse(map.containsKey(ObjectsPair.of("X", "Y")));
    map.clear();
    assertTrue(map.isEmpty());
    assertEquals(map.size(), 0);
    assertFalse(map.containsKey(ObjectsPair.of("B", "A")));
    assertFalse(map.containsKey("A", "B"));
  }

  public void testBasicOperations_strongKeys() {
    testBasicOperations(new HashMap2<String, String, String>(HashMap2.STRONG_KEYS));
  }

  public void testBasicOperations_weakKeys() {
    testBasicOperations(new HashMap2<String, String, String>(HashMap2.WEAK_KEYS));
  }

  private void testRemove(final Map2<String, String, String> map) {
    map.put(ObjectsPair.of("A", "B"), "Foo");
    map.put("B", "A", "Bar");
    assertEquals(map.remove(ObjectsPair.of("B", "A")), "Bar");
    assertEquals(map.size(), 1);
    assertEquals(map.remove(ObjectsPair.of("B", "A")), null);
    assertEquals(map.remove(ObjectsPair.of("X", "Y")), null);
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
    assertEquals(map.put(ObjectsPair.of("B", "A"), "Bar"), null);
    assertEquals(map.put(ObjectsPair.of("A", "B"), "Cow"), "Foo");
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
