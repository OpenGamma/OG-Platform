/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 *
 */
public class CouponPaymentDiscountFactorVisitor extends InstrumentDerivativeVisitorSameMethodAdapter<MulticurveProviderInterface, Double> {

  @Override
  public Double visit(InstrumentDerivative derivative, MulticurveProviderInterface data) {
    Coupon payment = (Coupon) derivative;
    return data.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }
  
  @Override
  public Double visit(InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curves to retrieve discount factors");
  }

}
