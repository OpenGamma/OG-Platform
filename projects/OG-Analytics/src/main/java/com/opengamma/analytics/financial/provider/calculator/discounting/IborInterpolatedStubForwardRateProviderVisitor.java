/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.IborInterpolatedStubCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.interestrate.payments.provider.ForwardRateProvider;
import com.opengamma.analytics.financial.interestrate.payments.provider.IborCompoundingInterpolationStubForwardRateProvider;
import com.opengamma.analytics.financial.interestrate.payments.provider.IborInterpolatedStubForwardRateProvider;

/**
 * Visitor for ibor-like interpolated stub coupons that return an instance of a ForwardRateProvider.
 */
public final class IborInterpolatedStubForwardRateProviderVisitor implements InterpolatedStubCouponVisitor<ForwardRateProvider<IborIndex>> {
  
  private static final IborInterpolatedStubForwardRateProviderVisitor INSTANCE = new IborInterpolatedStubForwardRateProviderVisitor();
  
  private IborInterpolatedStubForwardRateProviderVisitor() {
  }
  
  public static IborInterpolatedStubForwardRateProviderVisitor getInstance() {
    return INSTANCE;
  }

  @Override
  public ForwardRateProvider<IborIndex> visitIborCompoundingInterpolatedStub(IborInterpolatedStubCompoundingCoupon coupon) {
    return new IborCompoundingInterpolationStubForwardRateProvider(coupon);
  }

  @Override
  public ForwardRateProvider<IborIndex> visitIborInterpolatedStub(InterpolatedStubCoupon<DepositIndexCoupon<IborIndex>, IborIndex> coupon) {
    return new IborInterpolatedStubForwardRateProvider(coupon);
  }
  
  @Override
  public ForwardRateProvider<IborIndex> visitOvernightInterpolatedStub(InterpolatedStubCoupon<DepositIndexCoupon<IndexON>, IndexON> coupon) {
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operation.");
  }
}
