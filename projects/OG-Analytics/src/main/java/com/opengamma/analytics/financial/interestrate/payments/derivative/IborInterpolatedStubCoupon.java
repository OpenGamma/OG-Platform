/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.provider.calculator.discounting.InterpolatedStubCouponVisitor;

/**
 * 
 */
public final class IborInterpolatedStubCoupon extends InterpolatedStubCoupon<DepositIndexCoupon<IborIndex>, IborIndex> implements DepositIndexCoupon<IborIndex> {
   
  private IborInterpolatedStubCoupon(
      DepositIndexCoupon<IborIndex> fullCoupon,
      double firstInterpolatedTime,
      double firstInterpolatedYearFraction,
      double secondInterpolatedTime,
      double secondInterpolatedYearFraction) {
    super(fullCoupon, firstInterpolatedTime, firstInterpolatedYearFraction, secondInterpolatedTime, secondInterpolatedYearFraction);
  }
  
  public static IborInterpolatedStubCoupon from(
      final DepositIndexCoupon<IborIndex> fullCoupon,
      final double firstInterpolatedTime,
      final double firstInterpolatedYearFraction,
      final double secondInterpolatedTime,
      final double secondInterpolatedYearFraction) {
    return new IborInterpolatedStubCoupon(fullCoupon, firstInterpolatedTime, firstInterpolatedYearFraction, secondInterpolatedTime, secondInterpolatedYearFraction);
  }
  
  @Override
  public Coupon withNotional(double notional) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <S> S accept(InterpolatedStubCouponVisitor<S> visitor) {
    return visitor.visitIborInterpolatedStub(this);
  }
}
