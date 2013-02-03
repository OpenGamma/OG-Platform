/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateralmodel;

/**
 * Enumerate the types of rounding that can be applied to the amount of a collateral all
 */
public enum CollateralRoundingConvention {
  /**
   * Round the collateral amount called for up to the minimum transfer amount
   * e.g. if the minimum transfer amount is $1m and the collateral call is for $3.2m, the call is rounded up to $4m
   */
  UP,
  /**
   * Round the collateral amount called for down to the minimum transfer amount
   * e.g. if the minimum transfer amount is $1m and the collateral call is for $3.2m, the call is rounded down to $3m 
   */
  DOWN;

  // TODO : Check that this convention is correct
}
