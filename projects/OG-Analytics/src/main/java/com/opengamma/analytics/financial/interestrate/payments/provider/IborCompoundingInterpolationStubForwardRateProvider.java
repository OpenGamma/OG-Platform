/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.IborInterpolatedStubCompoundingCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Forward rate provider for compounding IBOR interpolated stubs.
 */
public final class IborCompoundingInterpolationStubForwardRateProvider implements ForwardRateProvider<IborIndex> {
  
 
  private final IborInterpolatedStubCompoundingCoupon _coupon;
  
  public IborCompoundingInterpolationStubForwardRateProvider(IborInterpolatedStubCompoundingCoupon coupon) {
    _coupon = coupon;
  }
  
  public <T extends DepositIndexCoupon<IborIndex>> double getRate(
      final MulticurveProviderInterface multicurves,
      final T coupon,
      final double fullFixingPeriodStartTime,
      final double fullFixingPeriodEndTime,
      final double fullFixingPeriodYearFraction) {
    
    double[] fixingPeriodStartTimes = _coupon.getFullCoupon().getFixingPeriodStartTimes();
    
    double forward;
    
    if (Double.compare(fixingPeriodStartTimes[0], fullFixingPeriodStartTime) == 0) {
      IborIndex index = _coupon.getFullCoupon().getIndex();
      double forwardInterpStart = multicurves.getSimplyCompoundForwardRate(index, fullFixingPeriodStartTime, _coupon.getFirstInterpolatedTime(), _coupon.getFirstInterpolatedYearFraction());
      double forwardInterpEnd = multicurves.getSimplyCompoundForwardRate(index, fullFixingPeriodStartTime, _coupon.getSecondInterpolatedTime(), _coupon.getSecondInterpolatedYearFraction());

      forward = forwardInterpStart + (forwardInterpEnd - forwardInterpStart) 
          * (fullFixingPeriodYearFraction - _coupon.getFirstInterpolatedYearFraction()) / 
          (_coupon.getSecondInterpolatedYearFraction() - _coupon.getFirstInterpolatedYearFraction());
      return Double.NaN;
    } else {
      forward = multicurves.getSimplyCompoundForwardRate(_coupon.getFullCoupon().getIndex(), fullFixingPeriodStartTime, fullFixingPeriodEndTime, fullFixingPeriodYearFraction);
    }
    
    return forward;
  }
}
