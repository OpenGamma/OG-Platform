/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

/**
 * Test.
 */
public class EntitlementUtilsTest {

  @Test
  public void test_entitlementText() {
    String actual = EntitlementUtils.generateEntitlementString(true, "op", "module", "detail");
    assertEquals("op:module:detail", actual);
    actual = EntitlementUtils.generateEntitlementString(false, "op", "module", "detail");
    assertEquals("-op:module:detail", actual);
  }

}
