/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingBundleMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponONDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;

/**
 * Calculator of the present value curve sensitivity as multiple currency interest rate curve sensitivity.
 * @deprecated This class uses deprecated pricing methods.
 */
@Deprecated
public class PresentValueCurveSensitivityMCSCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityMCSCalculator s_instance = new PresentValueCurveSensitivityMCSCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityMCSCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  public PresentValueCurveSensitivityMCSCalculator() {
  }

  /** Discounting for cash instruments */
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  /** Discounting for fixed payments */
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  /** Discounting for fixed coupons */
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  /** Discounting for overnight index coupons */
  private static final CouponONDiscountingMethod METHOD_CPN_OIS = CouponONDiscountingMethod.getInstance();
  /** Discounting for ibor-type coupons */
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  /** Discounting for ibor-type coupons with spread */
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  /** Discounting for FRAs */
  private static final ForwardRateAgreementDiscountingBundleMethod METHOD_FRA = ForwardRateAgreementDiscountingBundleMethod.getInstance();
  /** Discounting for interest rate future transactions */
  private static final InterestRateFutureTransactionDiscountingMethod METHOD_IR_FUTURES_TRANSACTION = InterestRateFutureTransactionDiscountingMethod.getInstance();
  /** Discounting for interest rate future securities */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_IR_FUTURES_SECURITY = InterestRateFutureSecurityDiscountingMethod.getInstance();
  /** Discounting for FX spot and forwards */
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  /** Discounting for FX swaps */
  private static final ForexSwapDiscountingMethod METHOD_FXSWAP = ForexSwapDiscountingMethod.getInstance();
  /** Discounting for non-deliverable FX forwards */
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCash(final Cash cash, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(cash.getCurrency(), METHOD_DEPOSIT.presentValueCurveSensitivity(cash, curves));
  }

  // -----     Coupon     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitFixedPayment(final PaymentFixed coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_PAY_FIXED.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_FIXED.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponOIS(final CouponON coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_OIS.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_IBOR.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(fra.getCurrency(), METHOD_FRA.presentValueCurveSensitivity(fra, curves));
  }

  // -----     Futures     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(future.getCurrency(), METHOD_IR_FUTURES_TRANSACTION.presentValueCurveSensitivity(future, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(future.getCurrency(), METHOD_IR_FUTURES_SECURITY.presentValueCurveSensitivity(future, curves));
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle data) {
    MultipleCurrencyInterestRateCurveSensitivity sensi = new MultipleCurrencyInterestRateCurveSensitivity();
    for (final Payment p : annuity.getPayments()) {
      sensi = sensi.plus(p.accept(this, data));
    }
    return sensi;
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    final MultipleCurrencyInterestRateCurveSensitivity sensi1 = swap.getFirstLeg().accept(this, curves);
    final MultipleCurrencyInterestRateCurveSensitivity sensi2 = swap.getSecondLeg().accept(this, curves);
    return sensi1.plus(sensi2);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForex(final Forex derivative, final YieldCurveBundle data) {
    return METHOD_FOREX.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexSwap(final ForexSwap derivative, final YieldCurveBundle data) {
    return METHOD_FXSWAP.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final YieldCurveBundle data) {
    return METHOD_NDF.presentValueCurveSensitivity(derivative, data);
  }

}
