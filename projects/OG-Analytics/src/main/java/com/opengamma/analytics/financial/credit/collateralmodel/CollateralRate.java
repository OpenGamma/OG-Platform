/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateralmodel;

/**
 * Enumerate the rates at which a return is earnt on posted collateral
 */
public enum CollateralRate {

  /**
   * Euro Overnight Index Average rate
   */
  EONIA,
  /**
   * Federal Reserve funds rate
   */
  FEDFUNDS,
  /**
   * No return earned
   */
  NONE,
  /**
   * Other (user will have to specify)
   */
  OTHER;
}
