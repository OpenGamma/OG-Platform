/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.LockedAccountException;

import com.opengamma.util.PublicAPI;

/**
 * The status of a user account, either enabled, disabled or locked.
 */
@PublicAPI
public enum UserAccountStatus {

  /**
   * The account is enabled.
   */
  ENABLED,
  /**
   * The account is disabled.
   */
  DISABLED,
  /**
   * The account is locked.
   * This state is typically used when an attempt was made to access the account
   * with the wrong password.
   */
  LOCKED;

  /**
   * Checks the status and throws an exception if not enabled.
   * 
   * @throws DisabledAccountException if disabled
   * @throws LockedAccountException if locked
   */
  public void check() {
    if (this == DISABLED) {
      throw new DisabledAccountException("Account disabled");
    }
    if (this == LOCKED) {
      throw new LockedAccountException("Account locked");
    }
  }

}
