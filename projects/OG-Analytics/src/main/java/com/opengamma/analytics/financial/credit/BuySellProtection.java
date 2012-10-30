/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 *  Flag to indicate if we are a buyer or seller of credit protection
 */
public enum BuySellProtection {
  /**
   * BUY protection (pay the premium leg (including accrued premium if required) - and receive the protection leg)
   */
  BUY,
  /**
   * SELL protection (receive the premium leg (including accrued premium if required) and pay the protection leg
   */
  SELL;
}
