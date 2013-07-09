/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a mock/test user.
 */
public class MockUserFactoryBean extends SingletonFactoryBean<UserPrincipal> {

  /**
   * The user that should be used for entitlement checking.
   */
  public static final UserPrincipal TEST_USER;

  static {
    try {
      TEST_USER = new UserPrincipal("mockintegrationtestuser", InetAddress.getLocalHost().toString());
    } catch (UnknownHostException ex) {
      throw new OpenGammaRuntimeException("Could not initialize test user", ex);
    }
  }

  @Override
  protected UserPrincipal createObject() {
    return TEST_USER;
  }

}
