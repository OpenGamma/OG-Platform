/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import com.opengamma.OpenGammaRuntimeException;

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

  public static StandardCDSQuotingConvention parse(final String convention) {
    if (SPREAD.name().equals(convention)) {
      return SPREAD;
    } else if (POINTS_UPFRONT.name().equals(convention)) {
      return POINTS_UPFRONT;
    } else {
      throw new OpenGammaRuntimeException("Unknown cds quoting convention: " + convention);
    }
  }

}
