/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * 
 *
 * @author kirk
 */
public final class SealableUtils {
  private SealableUtils() {
  }
  
  public static void checkSealed(Sealable sealable) {
    if(sealable.isSealed()) {
      throw new IllegalStateException("Instance " + sealable + " has been sealed. Modifications not permitted.");
    }
  }

}
