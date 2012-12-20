/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

/**
 * The Long or short flag.
 * <p>
 * This could be a boolean flag, but that would have to be named "long" or
 * "short", which are reserved words in Java, hence the enum.
 */
public enum LongShort {

  /**
   * Long.
   */
  LONG,
  /**
   * Short.
   */
  SHORT;

  //-------------------------------------------------------------------------
  /**
   * Converts a boolean "is long" flag to the enum value.
   * 
   * @param isLong  the long flag, true for long, false for short
   * @return the equivalent enum, not null
   */
  public static LongShort ofLong(boolean isLong) {
    return isLong ? LONG : SHORT;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the type is "long".
   * 
   * @return true if long, false if short
   */
  public boolean isLong() {
    return this == LONG;
  }

  /**
   * Checks if the type is "short".
   * 
   * @return true if short, false if long
   */
  public boolean isShort() {
    return this == SHORT;
  }

}
