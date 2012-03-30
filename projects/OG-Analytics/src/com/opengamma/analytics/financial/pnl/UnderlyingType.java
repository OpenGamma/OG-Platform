/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

/**
 * 
 */
//TODO this is wrong
public enum UnderlyingType {
  /** Spot price */
  SPOT_PRICE,
  /** Spot volatility */
  SPOT_VOLATILITY,
  /** Implied volatility */
  IMPLIED_VOLATILITY,
  /** Interest rate */
  INTEREST_RATE,
  /** Cost of carry */
  COST_OF_CARRY,
  /** Strike */
  STRIKE,
  /** Time */
  TIME,
  /** Implied variance */
  IMPLIED_VARIANCE,
  /** Yield */
  YIELD,
  /** The yield curve */
  YIELD_CURVE,
  /** Bond yield */
  BOND_YIELD,
  /** Forward */
  FORWARD,
  /** Volatility surface */
  VOLATILITY_SURFACE
}
