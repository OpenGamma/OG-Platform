/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;

/**
 *  Class used to compute the price of a CMS coupon by swaption replication with SABR Hagan formula.
 */
public class CouponCMSSABRReplicationMethod {
  private static final CouponCMSSABRReplicationMethod INSTANCE = new CouponCMSSABRReplicationMethod();

  /** 
   * Method returning a default instance. The default integration interval is 1.00 (100%).
   * @return A default instance
   */
  public static CouponCMSSABRReplicationMethod getDefaultInstance() {
    return INSTANCE;
  }

  /**
   * The interval length on which the CMS replication is computed for cap. The range is [strike, strike+integrationInterval].
   */
  private double _integrationInterval;

  /** 
   * Default constructor. The default integration interval is 1.00 (100%).
   */
  private CouponCMSSABRReplicationMethod() {
    _integrationInterval = 1.00;
  }

  /**
   * Constructor of the CMS replication method with the integration range. 
   * @param integrationInterval Integration range.
   */
  public CouponCMSSABRReplicationMethod(final double integrationInterval) {
    _integrationInterval = integrationInterval;
  }

  /**
   * Compute the price of a CMS coupon by replication in SABR framework. 
   * The CMS coupon is priced as a cap with strike 0.0. The strike 0.0 is used as the rates are always >=0.0 in SABR.
   * @param cmsCoupon The CMS coupon.
   * @param sabrData The SABR and curve data.
   * @return The coupon price.
   */
  public double presentValue(final CouponCMS cmsCoupon, final SABRInterestRateDataBundle sabrData) {
    final CapFloorCMS cap0 = CapFloorCMS.from(cmsCoupon, 0.0, true);
    final CapFloorCMSSABRReplicationMethod method = new CapFloorCMSSABRReplicationMethod(_integrationInterval);
    final double priceCMSCoupon = method.presentValue(cap0, sabrData).getAmount();
    return priceCMSCoupon;
  }

  /**
   * Computes the present value sensitivity to the yield curves of a CMS coupon by replication in SABR framework. 
   * @param cmsCoupon The CMS coupon.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CouponCMS cmsCoupon, final SABRInterestRateDataBundle sabrData) {
    final CapFloorCMS cap0 = CapFloorCMS.from(cmsCoupon, 0.0, true);
    // A CMS coupon is priced as a cap with strike 0.
    final CapFloorCMSSABRReplicationMethod method = new CapFloorCMSSABRReplicationMethod(_integrationInterval);
    return method.presentValueSensitivity(cap0, sabrData);
  }

  /**
   * Computes the present value sensitivity to the SABR parameters of a CMS coupon by replication in SABR framework.
   * @param cmsCoupon The CMS coupon.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to SABR parameters.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CouponCMS cmsCoupon, final SABRInterestRateDataBundle sabrData) {
    final CapFloorCMS cap0 = CapFloorCMS.from(cmsCoupon, 0.0, true);
    // A CMS coupon is priced as a cap with strike 0.
    final CapFloorCMSSABRReplicationMethod method = new CapFloorCMSSABRReplicationMethod(_integrationInterval);
    return method.presentValueSABRSensitivity(cap0, sabrData);
  }

  /**
   * Gets the integration interval.
   * @return The integration interval.
   */
  public double getIntegrationInterval() {
    return _integrationInterval;
  }

  /**
   * Sets the integration interval.
   * @param integrationInterval The integration interval.
   */
  public void setIntegrationInterval(final double integrationInterval) {
    this._integrationInterval = integrationInterval;
  }

}
