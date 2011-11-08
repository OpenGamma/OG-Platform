/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;
/**
 * The Floating rate type.
 */
public enum FloatingRateType {
  /**
   * Ibor
   */
  IBOR,
  /**
   * Cms
   */
  CMS,
  /**
   * Ois
   */
  OIS;
  
  //-------------------------------------------------------------------------
  /**
   * Checks if the type is "IBOR".
   * 
   * @return true if IBOR, false otherwise.
   */
  public boolean isIbor() {
    return this == IBOR;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Checks if the type is "CMS".
   * 
   * @return true if CMS, false otherwise.
   */
  public boolean isCms() {
    return this == CMS;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Checks if the type is "OIS".
   * 
   * @return true if OIS, false otherwise.
   */
  public boolean isOis() {
    return this == OIS;
  }
  
}
