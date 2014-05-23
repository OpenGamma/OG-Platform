/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import com.google.common.base.CaseFormat;
import com.opengamma.util.PublicSPI;

/**
 * Exception thrown when mutating a role.
 */
@PublicSPI
public enum RoleFormError {

  /**
   * The role name was missing.
   */
  ROLENAME_MISSING,
  /**
   * The role name was too short.
   */
  ROLENAME_TOO_SHORT,
  /**
   * The role name was too long.
   */
  ROLENAME_TOO_LONG,
  /**
   * The role name was invalid.
   */
  ROLENAME_INVALID,
  /**
   * The role name is already in use.
   */
  ROLENAME_ALREADY_IN_USE,
  /**
   * The password was missing.
   */
  DESCRIPTION_MISSING,
  /**
   * The password was too short.
   */
  DESCRIPTION_TOO_LONG,
  /**
   * The password was invalid.
   */
  DESCRIPTION_INVALID,
  /**
   * An unexpected error.
   */
  UNEXPECTED;

  //-------------------------------------------------------------------------
  /**
   * Gets the error code in lowerCamel format.
   * 
   * @return the error code, not null
   */
  public String toLowerCamel() {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
  }

}
