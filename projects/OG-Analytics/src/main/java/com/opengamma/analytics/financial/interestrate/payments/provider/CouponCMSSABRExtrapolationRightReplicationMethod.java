/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;


/**
 *  Class used to compute the price of a CMS coupon by swaption replication on a SABR formula with extrapolation.
 *  Reference: Hagan, P. S. (2003). Convexity conundrums: Pricing CMS swaps, caps, and floors. Wilmott Magazine, March, pages 38--44.
 *  OpenGamma implementation note: Replication pricing for linear and TEC format CMS, Version 1.2, March 2011.
 *  OpenGamma implementation note for the extrapolation: Smile extrapolation, version 1.2, May 2011.
 */
public class CouponCMSSABRExtrapolationRightReplicationMethod extends CouponCMSSABRReplicationGenericMethod {

  /** 
   * Default constructor. The default integration interval is 1.00 (100%).
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public CouponCMSSABRExtrapolationRightReplicationMethod(double cutOffStrike, double mu) {
    super(new CapFloorCMSSABRExtrapolationRightReplicationMethod(cutOffStrike, mu));
  }

}
