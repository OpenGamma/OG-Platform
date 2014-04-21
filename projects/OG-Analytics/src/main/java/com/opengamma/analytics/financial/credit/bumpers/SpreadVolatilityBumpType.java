/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

/**
 * Enumerate the types of bumps that can be applied to the spread volatility (to compute spread volatility sensitivities in CDS Swaption contracts)
 *@deprecated this will be deleted 
 */
@Deprecated
public enum SpreadVolatilityBumpType {
  /**
   * sigma -> sigma + bump
   */
  ADDITIVE,
  /**
   * sigma -> sigma x (1 + bump)
   */
  MULTIPLICATIVE;

}
