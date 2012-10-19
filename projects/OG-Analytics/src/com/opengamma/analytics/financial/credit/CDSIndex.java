/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Enumerate the different CDS indices
 */
public enum CDSIndex {
  /**
   * CDX index
   */
  CDX,
  /**
   * CDX High Yield index
   */
  CDX_HY,
  /**
   * CDX High Vol index
   */
  CDX_HiVol,
  /**
   * iTraxx index
   */
  iTraxx;

  // TODO : Add the rest of the index names (need to work out how to include NA, IG etc)
}
