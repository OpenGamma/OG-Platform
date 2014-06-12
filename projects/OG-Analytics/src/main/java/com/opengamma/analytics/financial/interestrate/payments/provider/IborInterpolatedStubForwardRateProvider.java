/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.Map.Entry;

import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * Forward rate provider for ibor-like interpolated stub coupons.
 */
public final class IborInterpolatedStubForwardRateProvider implements ForwardRateProvider<IborIndex> {
  
  private final InterpolatedStubCoupon<DepositIndexCoupon<IborIndex>, IborIndex> _coupon;
  
  
  public IborInterpolatedStubForwardRateProvider(InterpolatedStubCoupon<DepositIndexCoupon<IborIndex>, IborIndex> coupon) {
    _coupon = coupon;
  }
  
  public <T extends DepositIndexCoupon<IborIndex>> double getRate(
      final MulticurveProviderInterface multicurves,
      final T coupon,
      final double fixingPeriodStartTime,
      final double fixingPeriodEndTime,
      final double fixingPeriodYearFraction) {
    //IborIndex index = coupon.getIndex();
    MulticurveProviderDiscount disc = (MulticurveProviderDiscount) multicurves;            
    IborIndex firstStubIndex = resolveMatchingIndexFromCurveSet(disc, _coupon.getFirstStubIndex());
    IborIndex secondStubIndex = resolveMatchingIndexFromCurveSet(disc, _coupon.getSecondStubIndex());
    
    double forwardInterpStart = multicurves.getSimplyCompoundForwardRate(firstStubIndex, 
                                                                         fixingPeriodStartTime, 
                                                                         _coupon.getFirstInterpolatedTime(), 
                                                                         _coupon.getFirstInterpolatedYearFraction());
    
    double forwardInterpEnd = multicurves.getSimplyCompoundForwardRate(secondStubIndex, 
                                                                       fixingPeriodStartTime, 
                                                                       _coupon.getSecondInterpolatedTime(), 
                                                                       _coupon.getSecondInterpolatedYearFraction());

    double forward = forwardInterpStart + (forwardInterpEnd - forwardInterpStart) 
        * (fixingPeriodYearFraction - _coupon.getFirstInterpolatedYearFraction()) / (_coupon.getSecondInterpolatedYearFraction() - _coupon.getFirstInterpolatedYearFraction());
    
    return forward;
    
  }

  private IborIndex resolveMatchingIndexFromCurveSet(MulticurveProviderDiscount disc, IborIndex index) {
    IborIndex matchedIndex = null;
    for (Entry<IborIndex, YieldAndDiscountCurve> fwdCurve : disc.getForwardIborCurves().entrySet()) {
      IborIndex idx = fwdCurve.getKey();
      Currency ccy = idx.getCurrency();
      Period curveTenor = idx.getTenor();      
      if (ccy.equals(index.getCurrency()) && curveTenor.equals(index.getTenor())) {
        matchedIndex = idx;
      }
    }
    if (matchedIndex == null) {
      throw new OpenGammaRuntimeException("Unable to resolve a curve from the multicurve setup for: " + index.getName());
    }
    return matchedIndex;
  }
}
