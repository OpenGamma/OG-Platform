/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples;

import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a mock/test user.
 */
public class ExampleUserFactoryBean extends SingletonFactoryBean<UserPrincipal> {

  @Override
  protected UserPrincipal createObject() {
    return ExampleServerConstants.TEST_USER;
  }
}
