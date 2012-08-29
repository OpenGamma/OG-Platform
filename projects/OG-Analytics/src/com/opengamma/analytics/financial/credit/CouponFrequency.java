/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Specify the frequency of premium payments 
 */
public enum CouponFrequency {
  /**
   * Monthly premium payments
   */
  MONTHLY,
  /**
   * Quarterly premium payments (applicable to standard CDS contracts)
   */
  QUARTERLY,
  /**
   * Semi-annual premium payments
   */
  SEMIANNUAL,
  /**
   * Annual premium payments
   */
  ANNUAL;

  // TODO : Replace this with a more comprehensive list of payment frequencies
}
