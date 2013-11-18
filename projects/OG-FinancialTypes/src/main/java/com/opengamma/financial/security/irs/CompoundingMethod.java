/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

/**
 * Compounding methodology
 */
public enum CompoundingMethod {

  /**
   * Flat.
   */
  FLAT,

  /**
   * No compounding.
   */
  NONE,

  /**
   * Straight compounding.
   */
  STRAIGHT,

  /**
   * Spread exclusive compounding.
   */
  SPREAD_EXCLUSIVE

}
