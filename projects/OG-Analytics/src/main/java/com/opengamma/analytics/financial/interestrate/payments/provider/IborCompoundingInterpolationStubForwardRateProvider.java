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
import com.opengamma.analytics.financial.interestrate.payments.derivative.IborInterpolatedStubCompoundingCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;

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
    
    MulticurveProviderDiscount disc = (MulticurveProviderDiscount) multicurves;
    IborIndex firstStubIndex = resolveMatchingIndexFromCurveSet(disc, _coupon.getFirstStubIndex());
    IborIndex secondStubIndex = resolveMatchingIndexFromCurveSet(disc, _coupon.getSecondStubIndex());
        
    double forward;
    
    if (Double.compare(fixingPeriodStartTimes[0], fullFixingPeriodStartTime) == 0) {
      //IborIndex index = _coupon.getFullCoupon().getIndex();
      double forwardInterpStart = multicurves.getSimplyCompoundForwardRate(firstStubIndex, fullFixingPeriodStartTime, _coupon.getFirstInterpolatedTime(), _coupon.getFirstInterpolatedYearFraction());
      double forwardInterpEnd = multicurves.getSimplyCompoundForwardRate(secondStubIndex, fullFixingPeriodStartTime, _coupon.getSecondInterpolatedTime(), _coupon.getSecondInterpolatedYearFraction());

      forward = forwardInterpStart + (forwardInterpEnd - forwardInterpStart) 
          * (fullFixingPeriodYearFraction - _coupon.getFirstInterpolatedYearFraction()) / 
          (_coupon.getSecondInterpolatedYearFraction() - _coupon.getFirstInterpolatedYearFraction());
      //return Double.NaN;
    } else {
      forward = multicurves.getSimplyCompoundForwardRate(_coupon.getFullCoupon().getIndex(), fullFixingPeriodStartTime, fullFixingPeriodEndTime, fullFixingPeriodYearFraction);
    }    
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
