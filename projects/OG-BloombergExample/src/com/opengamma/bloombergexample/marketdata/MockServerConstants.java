/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.marketdata;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.UserPrincipal;

/**
 * 
 *
 */
public class MockServerConstants {

  /**
   * The user that should be used for entitlement checking
   */
  public static final UserPrincipal TEST_USER;
  
  /**
   * The topic subscription requests should be made to.
   */
  public static final String SUBSCRIPTION_REQUEST_TOPIC = "MockSubscriptionRequestTopic";
  
  /**
   * The topic entitlement requests should be made to.
   */
  public static final String ENTITLEMENT_REQUEST_TOPIC = "MockEntitlementRequestTopic";
  
  static {
    try {
      TEST_USER = new UserPrincipal("mockintegrationtestuser", InetAddress.getLocalHost().toString());
    } catch (UnknownHostException e) {
      throw new OpenGammaRuntimeException("Could not initialize test user", e);
    }
  }

}
