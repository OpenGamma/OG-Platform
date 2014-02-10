/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Forward rate provider for overnight interpolated stub coupons.
 */
public final class OvernightInterpolatedStubForwardRateProvider implements ForwardRateProvider<IndexON> {

  private final InterpolatedStubCoupon<DepositIndexCoupon<IndexON>, IndexON> _coupon;
  
  public OvernightInterpolatedStubForwardRateProvider(final InterpolatedStubCoupon<DepositIndexCoupon<IndexON>, IndexON> coupon) {
    _coupon = coupon;
  }
  
  @Override
  public <T extends DepositIndexCoupon<IndexON>> double getRate(
      final MulticurveProviderInterface multicurves,
      final T coupon,
      final double fixingPeriodStartTime,
      final double fixingPeriodEndTime,
      double fixingPeriodYearFraction) {
    IndexON index = coupon.getIndex();
    double forwardInterpStart = multicurves.getSimplyCompoundForwardRate(index, fixingPeriodStartTime, _coupon.getFirstInterpolatedTime(), _coupon.getFirstInterpolatedYearFraction());
    double forwardInterpEnd = multicurves.getSimplyCompoundForwardRate(index, fixingPeriodStartTime, _coupon.getSecondInterpolatedTime(), _coupon.getSecondInterpolatedYearFraction());

    double forward = forwardInterpStart + (forwardInterpEnd - forwardInterpStart) 
        * (fixingPeriodYearFraction - _coupon.getFirstInterpolatedYearFraction()) / (_coupon.getSecondInterpolatedYearFraction() - _coupon.getFirstInterpolatedYearFraction());
    
    return forward;
  }
}
