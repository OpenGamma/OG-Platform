/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Convention for computing amount of time between two cashflows (will replace with a more detailed structure in due course)
 */
public enum DayCountFraction {
  /**
   * ACT/360 (applicable to standard CDS contracts)
   */
  ACT360;
}
