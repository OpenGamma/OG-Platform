/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.collateralmodel;

/**
 * Enumerate the frequency with which collateral (margin) calls can be made 
 */
public enum MarginCallFrequency {
  /**
   * 
   */
  INTRADAY,
  /**
   * 
   */
  DAILY,
  /**
   * 
   */
  WEEKLY,
  /**
   * 
   */
  BIWEEKLY,
  /**
   * 
   */
  MONTHLY,
  /**
   * 
   */
  BIMONTHLY,
  /**
   * 
   */
  QUARTERLY,
  /**
   * 
   */
  SEMIANNUAL,
  /**
   * 
   */
  YEARLY,
  /**
   * 
   */
  OTHER;
}
