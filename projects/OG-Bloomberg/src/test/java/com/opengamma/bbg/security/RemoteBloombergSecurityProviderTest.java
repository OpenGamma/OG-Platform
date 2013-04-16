/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.security;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.opengamma.bbg.AbstractRemoteProviderTest;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Base case for testing BloombergSecuritySource.
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteBloombergSecurityProviderTest extends BloombergSecurityProviderTest {

  @BeforeTest
  public static void setupSuite() {
    AbstractRemoteProviderTest.setupSuite();
  }

  protected SecurityProvider createSecurityProvider() throws Exception {
    return AbstractRemoteProviderTest.getRemoteProviderUtils().getSecurityProviderBloomberg();
  }

  protected void stopSecurityProvider(SecurityProvider provider) throws Exception {
  }

}
