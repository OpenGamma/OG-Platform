/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test CompareUtils.
 */
public class CompareUtilsTest {

  @Test
  public void test_max() {
    assertEquals(null, CompareUtils.<String>max(null, null));
    assertEquals("A", CompareUtils.max(null, "A"));
    assertEquals("A", CompareUtils.max("A", null));
    Integer a = new Integer(1); // need to use new, not autoboxing
    Integer b = new Integer(1); // need to use new, not autoboxing
    assertSame(a, CompareUtils.max(a, b));  // as we test for same here
    assertEquals("B", CompareUtils.max("A", "B"));
  }

  @Test
  public void test_min() {
    assertEquals(null, CompareUtils.<String>min(null, null));
    assertEquals("A", CompareUtils.min(null, "A"));
    assertEquals("A", CompareUtils.min("A", null));
    Integer a = new Integer(1); // need to use new, not autoboxing
    Integer b = new Integer(1); // need to use new, not autoboxing
    assertSame(a, CompareUtils.min(a, b));  // as we test for same here
    assertEquals("A", CompareUtils.min("A", "B"));
  }

  @Test
  public void testCompareWithNull() {
    assertTrue(CompareUtils.compareWithNull(null, null) == 0);
    assertTrue(CompareUtils.compareWithNull(null, "Test") < 0);
    assertTrue(CompareUtils.compareWithNull("Test", null) > 0);
    assertTrue(CompareUtils.compareWithNull("Test", "Test") == 0);
    assertTrue(CompareUtils.compareWithNull("AAAA", "BBBB") == "AAAA".compareTo("BBBB"));
  }

  @Test
  public void testCompareWithNullHigh() {
    assertTrue(CompareUtils.compareWithNullHigh(null, null) == 0);
    assertTrue(CompareUtils.compareWithNullHigh(null, "Test") > 0);
    assertTrue(CompareUtils.compareWithNullHigh("Test", null) < 0);
    assertTrue(CompareUtils.compareWithNullHigh("Test", "Test") == 0);
    assertTrue(CompareUtils.compareWithNullHigh("AAAA", "BBBB") == "AAAA".compareTo("BBBB"));
  }

}
