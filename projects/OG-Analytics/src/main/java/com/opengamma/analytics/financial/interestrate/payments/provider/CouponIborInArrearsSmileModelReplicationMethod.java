/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class CouponIborInArrearsSmileModelReplicationMethod {

  private final CapFloorIborInArrearsSmileModelCapGenericReplicationMethod _method;

  /**
   * @param smileFunction Interpolated and extrapolated smile
   */
  public CouponIborInArrearsSmileModelReplicationMethod(final InterpolatedSmileFunction smileFunction) {
    _method = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(smileFunction);
  }

  /**
   * Computes the present value of an Ibor coupon in arrears by replication. The coupon is price as an cap with strike 0.
   * @param coupon Ibor coupon in arrears
   * @param curves The curves
   * @return The present value
   */
  public MultipleCurrencyAmount presentValue(final CouponIbor coupon, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(curves, "curves");
    CapFloorIbor cap0 = CapFloorIbor.from(coupon, 0.0, true);
    return _method.presentValue(cap0, curves);
  }
}
