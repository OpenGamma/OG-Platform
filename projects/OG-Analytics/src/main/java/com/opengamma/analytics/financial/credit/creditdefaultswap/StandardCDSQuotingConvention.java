/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

/**
 * Enumerate the types of quoting conventions in the marketplace for standard (post big-bang) CDS's
 */
public enum StandardCDSQuotingConvention {
  /**
   * Quote is a spread level (in bps)
   */
  SPREAD,

  /**
   * Quote is a percentage of the notional amount paid upfront
   */
  POINTS_UPFRONT;

}
