/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDates;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFlatCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;

/**
 *
 */
public class CouponPaymentYearFractionVisitor extends InstrumentDerivativeVisitorAdapter<Void, Double> {

  @Override
  public Double visitCouponFixed(final CouponFixed payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborAverage(final CouponIborAverage payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborCompounding(final CouponIborCompounding payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponOIS(final CouponON payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponONCompounded(final CouponONCompounded payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponCMS(final CouponCMS payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborAverageFixingDates(final CouponIborAverageFixingDates payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborAverageCompounding(final CouponIborAverageCompounding payment) {
    return payment.getPaymentYearFraction();
  }

  @Override
  public Double visitCouponIborFlatCompoundingSpread(final CouponIborFlatCompoundingSpread payment) {
    return payment.getPaymentYearFraction();
  }
}
