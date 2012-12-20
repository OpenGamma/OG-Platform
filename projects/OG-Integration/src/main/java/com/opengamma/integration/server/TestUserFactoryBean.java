/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a demo/test user.
 */
public class TestUserFactoryBean extends SingletonFactoryBean<UserPrincipal> {

  /**
   * The user that should be used for entitlement checking.
   */
  public static final UserPrincipal TEST_USER;

  static {
    try {
      TEST_USER = new UserPrincipal("testuser", InetAddress.getLocalHost().toString());
    } catch (UnknownHostException e) {
      throw new OpenGammaRuntimeException("Could not initialize test user", e);
    }
  }

  @Override
  protected UserPrincipal createObject() {
    return TEST_USER;
  }

}
