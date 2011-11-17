/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;

/**
 *  Class used to compute the price of a CMS coupon by swaption replication on a SABR formula with extrapolation.
 *  Reference: Hagan, P. S. (2003). Convexity conundrums: Pricing CMS swaps, caps, and floors. Wilmott Magazine, March, pages 38--44.
 *  OpenGamma implementation note: Replication pricing for linear and TEC format CMS, Version 1.2, March 2011.
 *  OpenGamma implementation note for the extrapolation: Smile extrapolation, version 1.2, May 2011.
 */
public class CouponCMSSABRExtrapolationRightReplicationMethod {

  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;

  /** 
   * Default constructor. The default integration interval is 1.00 (100%).
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public CouponCMSSABRExtrapolationRightReplicationMethod(double cutOffStrike, double mu) {
    _mu = mu;
    _cutOffStrike = cutOffStrike;
  }

  /**
   * Compute the price of a CMS coupon by replication in the SABR framework with extrapolation on the right.
   * @param cmsCoupon The CMS coupon.
   * @param sabrData The SABR and curve data.
   * @return The coupon price.
   */
  public double presentValue(CouponCMS cmsCoupon, SABRInterestRateDataBundle sabrData) {
    CapFloorCMS cap0 = CapFloorCMS.from(cmsCoupon, 0.0, true);
    // A CMS coupon is priced as a cap with strike 0.
    CapFloorCMSSABRExtrapolationRightReplicationMethod method = new CapFloorCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    double priceCMSCoupon = method.presentValue(cap0, sabrData).getAmount();
    return priceCMSCoupon;
  }

  /**
   * Computes the present value sensitivity to the yield curves of a CMS coupon by replication in the SABR framework with extrapolation on the right.
   * @param cmsCoupon The CMS coupon.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(CouponCMS cmsCoupon, SABRInterestRateDataBundle sabrData) {
    CapFloorCMS cap0 = CapFloorCMS.from(cmsCoupon, 0.0, true);
    // A CMS coupon is priced as a cap with strike 0.
    CapFloorCMSSABRExtrapolationRightReplicationMethod method = new CapFloorCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    return method.presentValueSensitivity(cap0, sabrData);
  }

}
