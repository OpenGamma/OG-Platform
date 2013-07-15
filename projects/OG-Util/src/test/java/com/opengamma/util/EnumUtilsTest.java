/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.math.RoundingMode;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EnumUtilsTest {

  public void test_safeValueOf() {
    assertEquals(RoundingMode.FLOOR, EnumUtils.safeValueOf(RoundingMode.class, "FLOOR"));
    assertEquals(null, EnumUtils.safeValueOf(RoundingMode.class, null));
    assertEquals(null, EnumUtils.<RoundingMode>safeValueOf(null, "FLOOR"));
    assertEquals(null, EnumUtils.safeValueOf(RoundingMode.class, "RUBBISH"));
  }

}
