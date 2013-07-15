/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EntitlementUtilsTest {

  @Test
  public void test_entitlementText() {
    String actual = EntitlementUtils.generateEntitlementString(true, "op", "module", "detail");
    assertEquals("op:module:detail", actual);
    actual = EntitlementUtils.generateEntitlementString(false, "op", "module", "detail");
    assertEquals("-op:module:detail", actual);
  }

}
