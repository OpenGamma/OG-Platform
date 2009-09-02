package com.opengamma.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CompareUtilsTest {

  @Test
  public void testEqualsWithNull() {
    assertTrue(CompareUtils.equalsWithNull(null, null));
    assertFalse(CompareUtils.equalsWithNull(null, "Test"));
    assertFalse(CompareUtils.equalsWithNull("Test", null));
    assertTrue(CompareUtils.equalsWithNull("Test", "Test"));
  }

  @Test
  public void testCompareWithNull() {
    assertTrue(CompareUtils.compareWithNull(null, null) == 0);
    assertTrue(CompareUtils.compareWithNull(null, "Test") < 0);
    assertTrue(CompareUtils.compareWithNull("Test", null) > 0);
    assertTrue(CompareUtils.compareWithNull("Test", "Test") == 0);
    assertTrue(CompareUtils.compareWithNull("AAAA", "BBBB") == "AAAA".compareTo("BBBB"));
  }
}
