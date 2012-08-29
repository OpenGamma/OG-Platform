/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Specify the frequency of premium payments (will replace these in due course)
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
}
