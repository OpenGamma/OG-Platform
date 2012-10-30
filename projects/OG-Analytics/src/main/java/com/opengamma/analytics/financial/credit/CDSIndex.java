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
   * CDX.NA.IG North American Investment Grade index
   */
  CDXNAIG,
  /**
   * CDX.NA.HY North American High Yield index
   */
  CDXNAHY,
  /**
   * CDX.NA.XO North American Crossover Credits index
   */
  CDXNAXO,
  /**
   * CDX Emerging Market Sovereign Credits index
   */
  CDXEM,
  /**
   * CDX Emerging Market Sovereign and Corporate Credits index
   */
  CDXEMDIVERS,
  /**
   * CDX High Vol index
   */
  CDXHiVol,
  /**
   * iTraxx Europe Investment Grade index
   */
  iTraxx,
  /**
   * iTraxx Europe High Vol index
   */
  iTraxxHiVol,
  /**
   * iTraxx Europe Crossover Credits index
   */
  iTraxxXO,
  /**
   * iTraxx Japanese Investment Grade index
   */
  iTraxxJapan,
  /**
   * iTraxx Asian non-Japan Investment Grade index
   */
  iTraxxAsiaExJapan,
  /**
   * iTraxx Australian Investment Grade index
   */
  iTraxxAustralia;

  // TODO : Add the rest of the index names (need to work out how to include NA, IG etc)
}
