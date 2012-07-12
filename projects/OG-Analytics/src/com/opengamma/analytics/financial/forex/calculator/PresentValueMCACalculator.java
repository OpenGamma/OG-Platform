/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponOISDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public class PresentValueMCACalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueMCACalculator s_instance = new PresentValueMCACalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueMCACalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueMCACalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponOISDiscountingMethod METHOD_OIS = CouponOISDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FXSWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visitCash(final Cash deposit, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_DEPOSIT.presentValue(deposit, curves));
  }

  // -----     Coupon     ------

  @Override
  public MultipleCurrencyAmount visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_IBOR.presentValue(coupon, curves));
  }

  @Override
  public MultipleCurrencyAmount visitCouponFixed(final CouponFixed payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(payment.getFundingCurveName());
    return MultipleCurrencyAmount.of(payment.getCurrency(), payment.getAmount() * fundingCurve.getDiscountFactor(payment.getPaymentTime()));
  }

  @Override
  public MultipleCurrencyAmount visitCouponOIS(final CouponOIS payment, final YieldCurveBundle data) {
    return MultipleCurrencyAmount.of(METHOD_OIS.presentValue(payment, data));
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    MultipleCurrencyAmount pv = visit(annuity.getNthPayment(0), curves);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pv = pv.plus(visit(annuity.getNthPayment(loopp), curves));
    }
    return pv;
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyAmount visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    final MultipleCurrencyAmount pvFirst = visit(swap.getFirstLeg(), curves);
    final MultipleCurrencyAmount pvSecond = visit(swap.getSecondLeg(), curves);
    return pvSecond.plus(pvFirst);
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public MultipleCurrencyAmount visitForex(final Forex derivative, final YieldCurveBundle data) {
    return METHOD_FOREX.presentValue(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexSwap(final ForexSwap derivative, final YieldCurveBundle data) {
    return METHOD_FXSWAP.presentValue(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final YieldCurveBundle data) {
    return METHOD_NDF.presentValue(derivative, data);
  }

}
