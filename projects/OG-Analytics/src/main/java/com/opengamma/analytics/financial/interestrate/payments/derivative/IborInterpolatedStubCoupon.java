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
      IborIndex firstStubIndex,
      double secondInterpolatedTime,
      double secondInterpolatedYearFraction,
      IborIndex secondStubIndex) {
    super(fullCoupon, firstInterpolatedTime, firstInterpolatedYearFraction, firstStubIndex, secondInterpolatedTime, secondInterpolatedYearFraction, secondStubIndex);
  }
  
  public static IborInterpolatedStubCoupon from(
      final DepositIndexCoupon<IborIndex> fullCoupon,
      final double firstInterpolatedTime,
      final double firstInterpolatedYearFraction,
      final IborIndex firstStubIndex,
      final double secondInterpolatedTime,
      final double secondInterpolatedYearFraction,
      final IborIndex secondStubIndex) {
    return new IborInterpolatedStubCoupon(fullCoupon, firstInterpolatedTime, firstInterpolatedYearFraction, firstStubIndex, secondInterpolatedTime, secondInterpolatedYearFraction, secondStubIndex);
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
