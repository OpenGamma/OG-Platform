/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;

/**
 * Calculator of the last fixing start time.
 * For fixed coupon, it is 0.
 */
public final class LastFixingStartTimeCalculator extends InstrumentDerivativeVisitorAdapter<Object, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final LastFixingStartTimeCalculator INSTANCE = new LastFixingStartTimeCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static LastFixingStartTimeCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private LastFixingStartTimeCalculator() {
  }

  // -----     Deposit     ------

  //  @Override
  //  public Double visitCash(final Cash cash) {
  //    return cash.getEndTime();
  //  }

  @Override
  public Double visitDepositIbor(final DepositIbor deposit) {
    return deposit.getStartTime();
  }

  // -----     Payment/Coupon     ------

  @Override
  public Double visitCouponFixed(final CouponFixed payment) {
    return 0.0;
  }

  @Override
  public Double visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return 0.0;
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment) {
    return payment.getFixingPeriodStartTime();
  }

  //  @Override
  //  public Double visitCouponOIS(final CouponOIS payment) {
  //    return payment.getFixingPeriodEndTime();
  //  }

  // -----     Annuity     ------

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    double result = 0.0;
    for (int loopp = 0; loopp < annuity.getNumberOfPayments(); loopp++) {
      result = Math.max(result, annuity.getNthPayment(loopp).accept(this));
    }
    return result;
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  // -----     Swap     ------

  @Override
  public Double visitSwap(final Swap<?, ?> swap) {
    final double a = swap.getFirstLeg().accept(this);
    final double b = swap.getSecondLeg().accept(this);
    return Math.max(a, b);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visitSwap(swap);
  }

  // -----     Inflation     -----

  @Override
  public Double visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
    return coupon.getReferenceEndTime();
  }

  @Override
  public Double visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
    return coupon.getReferenceEndTime();
  }

  @Override
  public Double visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
    return coupon.getReferenceEndTime()[0];
  }

  @Override
  public Double visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
    return coupon.getReferenceEndTime()[0];
  }

  @Override
  public Double visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon) {
    return coupon.getReferenceEndTime();
  }

  @Override
  public Double visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon) {
    return coupon.getReferenceEndTime()[0];
  }

}
