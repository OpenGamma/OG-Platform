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
   * Defines a computation target type that consists of linear fixed-income instruments.
   */
  public static final ComputationTargetType LINEAR_FIXED_INCOME_INSTRUMENT_TYPE = FinancialSecurityTypes.CASH_SECURITY
      .or(FinancialSecurityTypes.FRA_SECURITY)
      .or(FinancialSecurityTypes.SWAP_SECURITY);

}
