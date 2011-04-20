/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.server;

import com.opengamma.examples.livedata.BloombergServerConstants;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a demo/test user.
 */
public class TestUserFactoryBean extends SingletonFactoryBean<UserPrincipal> {

  @Override
  protected UserPrincipal createObject() {
    return BloombergServerConstants.TEST_USER;
  }

}