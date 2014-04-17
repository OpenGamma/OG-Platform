/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 *
 */
public class CouponPaymentDiscountFactorVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, Double> {

  @Override
  public Double visitCouponFixed(final CouponFixed payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborAverage(final CouponIborAverage payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborCompounding(final CouponIborCompounding payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponOIS(final CouponON payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponONCompounded(final CouponONCompounded payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponCMS(final CouponCMS payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponFixedCompounding(final CouponFixedCompounding payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

  @Override
  public Double visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment, final MulticurveProviderInterface curves) {
    return curves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
  }

}
