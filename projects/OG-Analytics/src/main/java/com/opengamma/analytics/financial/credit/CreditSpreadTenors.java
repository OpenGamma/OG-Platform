/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Enumerate the tenors of market observed par CDS spreads quoted in the market.
 * 
 * Note that not all of these tenors correspond to liquid points observed in the market. However
 * the intention is for the user to choose a subset of these according to their requirements
 * when constructing spread term structures
 *@deprecated this will be deleted 
 */
@Deprecated
public enum CreditSpreadTenors {

  /**
   * 0M spot credit spread
   */
  _0M,
  /**
   * 1M credit spread
   */
  _1M,
  /**
   * 2M credit spread
   */
  _2M,
  /**
   * 3M credit spread
   */
  _3M,
  /**
   * 4M credit spread
   */
  _4M,
  /**
   * 5M credit spread
   */
  _5M,
  /**
   * 6M credit spread
   */
  _6M,
  /**
   * 9M credit spread
   */
  _9M,
  /**
   * 12M credit spread
   */
  _12M,
  /**
   * 1Y credit spread
   */
  _1Y,
  /**
   * 2Y credit spread
   */
  _2Y,
  /**
   * 3Y credit spread
   */
  _3Y,
  /**
   * 4Y credit spread
   */
  _4Y,
  /**
   * 5Y credit spread
   */
  _5Y,
  /**
   * 6Y credit spread
   */
  _6Y,
  /**
   * 7Y credit spread
   */
  _7Y,
  /**
   * 8Y credit spread
   */
  _8Y,
  /**
   * 9Y credit spread
   */
  _9Y,
  /**
   * 10Y credit spread
   */
  _10Y,
  /**
   * 12Y credit spread
   */
  _12Y,
  /**
   * 15Y credit spread
   */
  _15Y,
  /**
   * 20Y credit spread
   */
  _20Y,
  /**
   * 30Y credit spread
   */
  _30Y,
  /**
   * 40Y credit spread
   */
  _40Y,
  /**
   * 50Y credit spread
   */
  _50Y;

}
