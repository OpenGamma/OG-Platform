/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.server;

import com.opengamma.livedata.BloombergServerConstants;
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
