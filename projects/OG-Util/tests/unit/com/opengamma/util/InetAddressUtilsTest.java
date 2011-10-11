/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class InetAddressUtilsTest {

  public void test_strip() {
    assertNotNull(InetAddressUtils.getLocalHostName());
  }

}
