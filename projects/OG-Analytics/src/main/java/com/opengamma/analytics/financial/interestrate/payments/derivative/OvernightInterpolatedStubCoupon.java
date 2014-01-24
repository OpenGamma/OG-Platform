/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.InterpolatedStubCouponVisitor;

/**
 * 
 */
public final class OvernightInterpolatedStubCoupon extends InterpolatedStubCoupon<DepositIndexCoupon<IndexON>, IndexON> implements DepositIndexCoupon<IndexON> {

  public OvernightInterpolatedStubCoupon(
      DepositIndexCoupon<IndexON> fullCoupon,
      double firstInterpolatedTime,
      double firstInterpolatedYearFraction,
      double secondInterpolatedTime,
      double secondInterpolatedYearFraction) {
    super(fullCoupon, firstInterpolatedTime, firstInterpolatedYearFraction, secondInterpolatedTime, secondInterpolatedYearFraction);
  }

  @Override
  public Coupon withNotional(double notional) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public <S> S accept(InterpolatedStubCouponVisitor<S> visitor) {
    return null;
  }
}
