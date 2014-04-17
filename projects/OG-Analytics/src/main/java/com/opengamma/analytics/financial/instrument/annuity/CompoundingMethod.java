/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

/**
 * Compounding methods.
 */
public enum CompoundingMethod {
  
  /**
   * None.
   */
  NONE,
  
  /**
   * Flat.
   */
  FLAT,
  
  /**
   * Straight.
   */
  STRAIGHT,
  
  /**
   * Spread exclusive.
   */
  SPREAD_EXCLUSIVE
}
