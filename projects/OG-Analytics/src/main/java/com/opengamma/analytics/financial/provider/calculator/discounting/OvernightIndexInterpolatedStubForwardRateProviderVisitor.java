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
import com.opengamma.analytics.financial.interestrate.payments.provider.OvernightInterpolatedStubForwardRateProvider;

/**
 * Visitor for overnight interpolated stub coupons that return an instance of a ForwardRateProvider.
 */
public final class OvernightIndexInterpolatedStubForwardRateProviderVisitor implements InterpolatedStubCouponVisitor<ForwardRateProvider<IndexON>> {

  /**
   * Singleton instance.
   */
  private static final OvernightIndexInterpolatedStubForwardRateProviderVisitor INSTANCE = new OvernightIndexInterpolatedStubForwardRateProviderVisitor();
  
  /**
   * Singleton constructor.
   */
  private OvernightIndexInterpolatedStubForwardRateProviderVisitor() {
  }
  
  /**
   * Returns a singleton.
   * @return a singleton.
   */
  public static OvernightIndexInterpolatedStubForwardRateProviderVisitor getInstance() {
    return INSTANCE;
  }
  
  @Override
  public ForwardRateProvider<IndexON> visitIborCompoundingInterpolatedStub(IborInterpolatedStubCompoundingCoupon coupon) {
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operation.");
  }

  @Override
  public ForwardRateProvider<IndexON> visitIborInterpolatedStub(InterpolatedStubCoupon<DepositIndexCoupon<IborIndex>, IborIndex> coupon) {
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operation.");
  }

  @Override
  public ForwardRateProvider<IndexON> visitOvernightInterpolatedStub(InterpolatedStubCoupon<DepositIndexCoupon<IndexON>, IndexON> coupon) {
    return new OvernightInterpolatedStubForwardRateProvider(coupon);
  }
}
