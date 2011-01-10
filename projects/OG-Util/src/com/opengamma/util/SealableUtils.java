/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Utilities for managing {@code Sealable} classes.
 */
public final class SealableUtils {

  /**
   * Restrictive constructor.
   */
  private SealableUtils() {
  }

  /**
   * Checks if the class is currently sealed, throwing an exception if it is.
   * This is used by implementations at the start of modification methods.
   * @param sealable  the instance to check, not null
   */
  public static void checkSealed(Sealable sealable) {
    if (sealable.isSealed()) {
      throw new IllegalStateException("Instance " + sealable + " has been sealed. Modifications not permitted.");
    }
  }

}
