/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.FinancialSecurityTypes;

/**
 *
 */
public class InstrumentTypeHelper {

  /**
   * Defines a computation target type that consists of linear fixed-income instruments; cash, FRA and swaps
   */
  public static final ComputationTargetType LINEAR_FIXED_INCOME_INSTRUMENT_TYPE = FinancialSecurityTypes.CASH_SECURITY
      .or(FinancialSecurityTypes.FRA_SECURITY)
      .or(FinancialSecurityTypes.SWAP_SECURITY);

  /**
   * Defines a computation target type that consists of futures related to rates; bond futures, interest rate futures
   * and deliverable swap futures.
   */
  public static final ComputationTargetType RATE_FUTURES_INSTRUMENT_TYPE = FinancialSecurityTypes.BOND_FUTURE_SECURITY
      .or(FinancialSecurityTypes.INTEREST_RATE_FUTURE_SECURITY)
      .or(FinancialSecurityTypes.DELIVERABLE_SWAP_FUTURE_SECURITY);
}
