/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

/**
 * Enumerate the coupon rates that can be applied to Standard CDS contracts (European and North American)
 */
public enum StandardCDSCoupon {
  /**
   * 25bps contract (Europe)
   */
  _25bps,
  /**
   * 100bps contract (North America and Europe)
   */
  _100bps,
  /**
   * 125bps contract (not sure if this is actually traded)
   */
  _125bps,
  /**
   * 300bps contract (Europe)
   */
  _300bps,
  /**
   * 500bps contract (North America and Europe)
   */
  _500bps,
  /**
   * 750bps contract (Europe)
   */
  _750bps,
  /**
   * 1000bps contract (Europe)
   */
  _1000bps;
}
