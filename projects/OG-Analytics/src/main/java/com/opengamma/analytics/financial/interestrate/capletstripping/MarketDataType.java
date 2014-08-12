/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

/**
 * Enumeration to distinguish options quoted by price or (Black) volatility 
 */
public enum MarketDataType {
  /**
   * Price
   */
  PRICE,

  /**
   * Implied Volatility 
   */
  VOL

}
