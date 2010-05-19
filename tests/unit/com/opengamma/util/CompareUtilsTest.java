/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
