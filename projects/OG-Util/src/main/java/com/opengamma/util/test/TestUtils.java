/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.DefaultSecurityManager;

/**
 * Utility methods for working with tests.
 */
public final class TestUtils {

  /**
   * Restricted constructor.
   */
  private TestUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Sets up the security manager for testing.
   */
  public static void initSecurity() {
    try {
      SecurityUtils.getSecurityManager();
    } catch (UnavailableSecurityManagerException ex) {
      SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    }
  }

}
