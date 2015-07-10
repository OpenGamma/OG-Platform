/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.permission;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.shiro.authz.UnauthenticatedException;
import org.testng.annotations.Test;

import com.bloomberglp.blpapi.Identity;
import com.google.common.collect.Lists;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.SessionProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.INTEGRATION, enabled = false)
public class BloombergBpipeApplicationUserIdentityProviderTest {

  public void authorizedIdentity() {
    BloombergConnector connector = BloombergTestUtils.getBloombergBipeConnector();
    SessionProvider sessionProvider = new SessionProvider(connector, Lists.newArrayList(BloombergConstants.AUTH_SVC_NAME, BloombergConstants.MKT_DATA_SVC_NAME, BloombergConstants.REF_DATA_SVC_NAME));
    sessionProvider.start();
    BloombergBpipeApplicationUserIdentityProvider provider = new BloombergBpipeApplicationUserIdentityProvider(sessionProvider);

    Identity identity = provider.getIdentity();
    assertNotNull(identity);
    assertTrue(identity.isAuthorized(sessionProvider.getService(BloombergConstants.MKT_DATA_SVC_NAME)));
    assertTrue(identity.isAuthorized(sessionProvider.getService(BloombergConstants.REF_DATA_SVC_NAME)));

    sessionProvider.stop();
  }

  @Test(expectedExceptions = UnauthenticatedException.class)
  public void unauthorizedIdentity() {
    SessionProvider sessionProvider = null;
    try {
      BloombergConnector connector = BloombergTestUtils.getBloombergBipeConnector();
      connector.getSessionOptions().setAuthenticationOptions(BloombergConstants.AUTH_APP_PREFIX + "UnknownAppName");
      sessionProvider = new SessionProvider(connector, Lists.newArrayList(BloombergConstants.AUTH_SVC_NAME, BloombergConstants.MKT_DATA_SVC_NAME, BloombergConstants.REF_DATA_SVC_NAME));
      sessionProvider.start();
      BloombergBpipeApplicationUserIdentityProvider provider = new BloombergBpipeApplicationUserIdentityProvider(sessionProvider);

      provider.getIdentity();
    } finally {
      if (sessionProvider != null) {
        sessionProvider.stop();
      }
    }
  }

}
