/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

/**
 *  Class used to compute the price of a CMS coupon by swaption replication on a SABR formula.
 *  Reference: Hagan, P. S. (2003). Convexity conundrums: Pricing CMS swaps, caps, and floors. Wilmott Magazine, March, pages 38--44.
 *  OpenGamma implementation note: Replication pricing for linear and TEC format CMS, Version 1.2, March 2011.
 */
public final class CouponCMSSABRReplicationMethod extends CouponCMSSABRReplicationGenericMethod {

  /**
   * The method default instance.
   */
  private static final CouponCMSSABRReplicationMethod INSTANCE = new CouponCMSSABRReplicationMethod();

  /** 
   * Returns a default instance of the CMS cap/floor replication method. The default integration interval is 1.00 (100%).
   * @return The calculation method
   */
  public static CouponCMSSABRReplicationMethod getInstance() {
    return INSTANCE;
  }

  /** 
   * Default constructor. The default integration interval is 1.00 (100%).
   */
  private CouponCMSSABRReplicationMethod() {
    super(CapFloorCMSSABRReplicationMethod.getDefaultInstance());
  }

}
