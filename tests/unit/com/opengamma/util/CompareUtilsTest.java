/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CompareUtilsTest {

  @Test
  public void testCompareWithNull() {
    assertTrue(CompareUtils.compareWithNull(null, null) == 0);
    assertTrue(CompareUtils.compareWithNull(null, "Test") < 0);
    assertTrue(CompareUtils.compareWithNull("Test", null) > 0);
    assertTrue(CompareUtils.compareWithNull("Test", "Test") == 0);
    assertTrue(CompareUtils.compareWithNull("AAAA", "BBBB") == "AAAA".compareTo("BBBB"));
  }
}
