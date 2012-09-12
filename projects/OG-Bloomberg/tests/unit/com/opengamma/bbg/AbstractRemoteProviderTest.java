/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Abstract integration test accessing a remote provider.
 */
@Test
public abstract class AbstractRemoteProviderTest {

  private static RemoteProviderTestUtils s_testUtils;

  @BeforeTest
  public static synchronized void setupSuite() {
    if (s_testUtils == null) {
      s_testUtils = RemoteProviderTestUtils.INSTANCE;
    }
  }

  public static RemoteProviderTestUtils getRemoteProviderUtils() {
    return s_testUtils;
  }

}
