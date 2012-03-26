/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.server;

import com.opengamma.bloombergexample.marketdata.MockServerConstants;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a mock/test user.
 */
public class MockUserFactoryBean extends SingletonFactoryBean<UserPrincipal> {

  @Override
  protected UserPrincipal createObject() {
    return MockServerConstants.TEST_USER;
  }
}
