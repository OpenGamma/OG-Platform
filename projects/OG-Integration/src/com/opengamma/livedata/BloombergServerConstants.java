/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 *
 */
public class BloombergServerConstants {

  /**
   * The user that should be used for entitlement checking
   */
  public static final UserPrincipal TEST_USER;

  static {
    try {
      TEST_USER = new UserPrincipal("bbgintegrationtestuser", InetAddress.getLocalHost().toString());
    } catch (UnknownHostException e) {
      throw new OpenGammaRuntimeException("Could not initialize test user", e);
    }
  }

}
