/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Enumerate the convntions for dealing with cashflows that fall on non-business days 
 */
public enum BusinessDayConvention {
  /**
   * Following - adjust the cashflow date to the next valid business day (default for legacy CDS)
   */
  FOLLOWING,
  /**
   * Preceeding - adjust the cashflow date to the previous valid business day
   */
  PRECEEDING;

  // TODO : Add to this list
}
