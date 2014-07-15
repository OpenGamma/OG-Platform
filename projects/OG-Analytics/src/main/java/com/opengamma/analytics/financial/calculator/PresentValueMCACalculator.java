/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.method.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingBundleMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponONDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 * @deprecated The methods used in this calculator are deprecated.
 */
@Deprecated
public class PresentValueMCACalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, MultipleCurrencyAmount> {

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
  public PresentValueMCACalculator() {
  }

  /** Discounting for cash instruments */
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  /** Discounting for bills */
  private static final BillTransactionDiscountingMethod METHOD_BILL_TRANSACTION = BillTransactionDiscountingMethod.getInstance();
  /** Discounting for fixed payments */
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  /** Discounting for fixed couponds */
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  /** Discounting for overnight-indexed coupons */
  private static final CouponONDiscountingMethod METHOD_CPN_OIS = CouponONDiscountingMethod.getInstance();
  /** Discounting for ibor-type coupons */
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  /** Discounting for ibor-type coupons with spread */
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  /** Discounting for compounded ibor-type coupons */
  private static final CouponIborCompoundedDiscountingMethod METHOD_CPN_IBOR_COMP = CouponIborCompoundedDiscountingMethod.getInstance();
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

  // -----     Deposit     ------

  @Override
  public MultipleCurrencyAmount visitCash(final Cash deposit, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_DEPOSIT.presentValue(deposit, curves));
  }

  @Override
  public MultipleCurrencyAmount visitDepositCounterpart(final DepositCounterpart deposit, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_DEPOSIT.presentValue(deposit, curves));
  }

  // -----     Bill/Bond     ------

  @Override
  public MultipleCurrencyAmount visitBillTransaction(final BillTransaction bill, final YieldCurveBundle curves) {
    Validate.notNull(curves, "Curves");
    Validate.notNull(bill, "Bill");
    return MultipleCurrencyAmount.of(METHOD_BILL_TRANSACTION.presentValue(bill, curves));
  }

  // -----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyAmount visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_PAY_FIXED.presentValue(payment, curves));
  }

  @Override
  public MultipleCurrencyAmount visitCouponFixed(final CouponFixed payment, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_CPN_FIXED.presentValue(payment, curves));
  }

  @Override
  public MultipleCurrencyAmount visitCouponOIS(final CouponON payment, final YieldCurveBundle data) {
    return MultipleCurrencyAmount.of(METHOD_CPN_OIS.presentValue(payment, data));
  }

  @Override
  public MultipleCurrencyAmount visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_CPN_IBOR.presentValue(coupon, curves));
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_CPN_IBOR_SPREAD.presentValue(coupon, curves));
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborCompounding(final CouponIborCompounding coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_CPN_IBOR_COMP.presentValue(coupon, curves));
  }

  @Override
  public MultipleCurrencyAmount visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_FRA.presentValue(fra, curves));
  }

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_IR_FUTURES_TRANSACTION.presentValue(future, curves));
  }

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    return MultipleCurrencyAmount.of(METHOD_IR_FUTURES_SECURITY.presentValue(future, curves));
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    MultipleCurrencyAmount pv = annuity.getNthPayment(0).accept(this, curves);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pv = pv.plus(annuity.getNthPayment(loopp).accept(this, curves));
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
    final MultipleCurrencyAmount pvFirst = swap.getFirstLeg().accept(this, curves);
    final MultipleCurrencyAmount pvSecond = swap.getSecondLeg().accept(this, curves);
    return pvSecond.plus(pvFirst);
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  // -----     Forex     ------

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
