/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.InterpolatedStubCouponVisitor;

/**
 * Base class for interpolated stub coupon.
 * 
 * @param <C> The full coupon.
 * @param <I> The deposit index.
 */
public abstract class InterpolatedStubCoupon<C extends DepositIndexCoupon<I>, I extends IndexDeposit> extends Coupon {

  private final C _fullCoupon;
  
  private final double _firstInterpolatedTime;
  
  private final double _firstInterpolatedYearFraction;
  
  private final IborIndex _firstStubIndex;
  
  private final double _secondInterpolatedTime;
  
  private final double _secondInterpolatedYearFraction;
  
  private final IborIndex _secondStubIndex;
  
  protected InterpolatedStubCoupon(
      C fullCoupon,
      double firstInterpolatedTime,
      double firstInterpolatedYearFraction,
      IborIndex firstStubIndex,
      double secondInterpolatedTime,
      double secondInterpolatedYearFraction,
      IborIndex secondStubIndex) {
    super(fullCoupon.getCurrency(), fullCoupon.getPaymentTime(), fullCoupon.getPaymentYearFraction(), fullCoupon.getNotional());
    _fullCoupon = fullCoupon;
    _firstInterpolatedTime = firstInterpolatedTime;
    _firstInterpolatedYearFraction = firstInterpolatedYearFraction;
    _firstStubIndex = firstStubIndex;
    _secondInterpolatedTime = secondInterpolatedTime;
    _secondInterpolatedYearFraction = secondInterpolatedYearFraction;
    _secondStubIndex = secondStubIndex;
  }
  
  public C getFullCoupon() {
    return _fullCoupon;
  }
  
  public I getIndex() {
    return _fullCoupon.getIndex();
  }
  
  public double getFirstInterpolatedTime() {
    return _firstInterpolatedTime;
  }
  
  public double getFirstInterpolatedYearFraction() {
    return _firstInterpolatedYearFraction;
  }
  
  public IborIndex getFirstStubIndex() {
    return _firstStubIndex;
  }
  
  public double getSecondInterpolatedTime() {
    return _secondInterpolatedTime;
  }
  
  public double getSecondInterpolatedYearFraction() {
    return _secondInterpolatedYearFraction;
  }
  
  public IborIndex getSecondStubIndex() {
    return _secondStubIndex;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitInterpolatedStubCoupon(this, data);    
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitInterpolatedStubCoupon(this);
  }
  
  public abstract <S> S accept(InterpolatedStubCouponVisitor<S> visitor);
}
