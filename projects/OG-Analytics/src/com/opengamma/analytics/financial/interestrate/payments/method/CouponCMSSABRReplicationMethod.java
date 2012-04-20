/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.CouponCMS;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.util.money.CurrencyAmount;

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
  private final double _integrationInterval;
  /**
   * The method used to price CMS cap/floor.
   */
  private final CapFloorCMSSABRReplicationMethod _capFloorMethod;

  /** 
   * Default constructor. The default integration interval is 1.00 (100%) and the default cap/floor replication method is CapFloorCMSSABRReplicationMethod.
   */
  private CouponCMSSABRReplicationMethod() {
    _integrationInterval = 1.00;
    _capFloorMethod = new CapFloorCMSSABRReplicationMethod(_integrationInterval);
  }

  /**
   * Constructor of the CMS replication method with the integration range. 
   * @param integrationInterval Integration range.
   */
  public CouponCMSSABRReplicationMethod(final double integrationInterval) {
    _integrationInterval = integrationInterval;
    _capFloorMethod = new CapFloorCMSSABRReplicationMethod(_integrationInterval);
  }

  /**
   * Constructor of the CMS replication method with the integration range. 
   * @param capFloorMethod The method used to price CMS cap/floor.
   */
  public CouponCMSSABRReplicationMethod(final CapFloorCMSSABRReplicationMethod capFloorMethod) {
    _integrationInterval = 1.0;
    _capFloorMethod = capFloorMethod;
  }

  /**
   * Constructor of the CMS replication method with the integration range. 
   * @param capFloorMethod The method used to price CMS cap/floor.
   * @param integrationInterval Integration range.
   */
  public CouponCMSSABRReplicationMethod(final CapFloorCMSSABRReplicationMethod capFloorMethod, final double integrationInterval) {
    _integrationInterval = integrationInterval;
    _capFloorMethod = capFloorMethod;
  }

  /**
   * Compute the price of a CMS coupon by replication in SABR framework. 
   * The CMS coupon is priced as a cap with strike 0.0. The strike 0.0 is used as the rates are always >=0.0 in SABR.
   * @param cmsCoupon The CMS coupon.
   * @param sabrData The SABR and curve data.
   * @return The coupon price.
   */
  public CurrencyAmount presentValue(final CouponCMS cmsCoupon, final SABRInterestRateDataBundle sabrData) {
    final CapFloorCMS cap0 = CapFloorCMS.from(cmsCoupon, 0.0, true);
    final CurrencyAmount priceCMSCoupon = _capFloorMethod.presentValue(cap0, sabrData);
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
    return _capFloorMethod.presentValueCurveSensitivity(cap0, sabrData);
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
    return _capFloorMethod.presentValueSABRSensitivity(cap0, sabrData);
  }

  /**
   * Gets the integration interval.
   * @return The integration interval.
   */
  public double getIntegrationInterval() {
    return _integrationInterval;
  }

}
