/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition;

/**
 * Enumerate the different CDS indices
 */
public enum CDSIndex {
  /**
   * CDX.NA.IG North American Investment Grade index (trades on spread)
   */
  CDXNAIG,
  /**
   * CDX.NA.HY North American High Yield index (trades on price)
   */
  CDXNAHY,
  /**
   * CDX.NA.XO North American Crossover Credits index (a crossover credit is one which was previously IG but has since been downgraded)
   */
  CDXNAXO,
  /**
   * CDX.EM Emerging Market Sovereign Credits index (trades on price)
   */
  CDXEM,
  /**
   * CDX.EM.Diversified Emerging Market Sovereign and Corporate Credits index
   */
  CDXEMDIVERS,
  /**
   * CDX High Vol index (trades on spread)
   */
  CDXHVOL,
  /**
   * iTraxx Europe Investment Grade index (trades on spread) 
   */
  iTraxx,
  /**
   * iTraxx Europe High Vol index
   */
  iTraxxHVOL,
  /**
   * iTraxx Europe Crossover Credits index
   */
  iTraxxXO,
  /**
   * iTraxx Western Europe Sovereigns
   */
  iTraxxSovX,
  /**
   * iTraxx Japanese Investment Grade index (trades on spread)
   */
  iTraxxJapan,
  /**
   * iTraxx Asian non-Japan Investment Grade index (trades on spread)
   */
  iTraxxAsiaExJapan,
  /**
   * iTraxx Australian Investment Grade index (trades on spread)
   */
  iTraxxAustralia,
  /**
   * iTraxx Central & Eastern European, Middle Eastern and African countries (corporate and quasi-sovereign)
   */
  iTraxxCEEMEA,
  /**
   * General purpose bespoke index (constituents chosen by the user)
   */
  BESPOKE;

  // TODO : Add the rest of the index names (need to work out how to include NA, IG etc) and sort out which ones trade on spread/price
}
