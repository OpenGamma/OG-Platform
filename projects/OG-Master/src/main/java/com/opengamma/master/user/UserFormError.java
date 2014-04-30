/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import com.google.common.base.CaseFormat;
import com.opengamma.util.PublicSPI;

/**
 * Exception thrown when mutating a user.
 */
@PublicSPI
public enum UserFormError {

  /**
   * The user name was missing.
   */
  USERNAME_MISSING,
  /**
   * The user name was too short.
   */
  USERNAME_TOO_SHORT,
  /**
   * The user name was too long.
   */
  USERNAME_TOO_LONG,
  /**
   * The user name was invalid.
   */
  USERNAME_INVALID,
  /**
   * The user name is already in use.
   */
  USERNAME_ALREADY_IN_USE,
  /**
   * The password was missing.
   */
  PASSWORD_MISSING,
  /**
   * The password was too short.
   */
  PASSWORD_TOO_SHORT,
  /**
   * The password was too long.
   */
  PASSWORD_TOO_LONG,
  /**
   * The password was too weak.
   */
  PASSWORD_WEAK,
  /**
   * The email address was missing.
   */
  EMAIL_MISSING,
  /**
   * The email address was too long
   */
  EMAIL_TOO_LONG,
  /**
   * The email address was invalid.
   */
  EMAIL_INVALID,
  /**
   * The display name address was missing.
   */
  DISPLAYNAME_MISSING,
  /**
   * The display name was too long.
   */
  DISPLAYNAME_TOO_LONG,
  /**
   * The display name was invalid.
   */
  DISPLAYNAME_INVALID,
  /**
   * The locale was invalid.
   */
  LOCALE_INVALID,
  /**
   * The time zone was invalid.
   */
  TIMEZONE_INVALID,
  /**
   * The date style was invalid.
   */
  DATESTYLE_INVALID,
  /**
   * The time style was invalid.
   */
  TIMESTYLE_INVALID,
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
