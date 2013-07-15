/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JdkUtilsTest {

  public void test_strip() {
    assertEquals(BigDecimal.valueOf(0, 0), JdkUtils.stripTrailingZeros(BigDecimal.valueOf(0, 2)));
    assertEquals(BigDecimal.valueOf(1, 0), JdkUtils.stripTrailingZeros(BigDecimal.valueOf(1, 0)));
  }

}
