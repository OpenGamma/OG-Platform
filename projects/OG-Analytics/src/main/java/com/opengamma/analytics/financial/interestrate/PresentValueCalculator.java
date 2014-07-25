/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.method.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.method.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.method.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.method.DepositZeroDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingBundleMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedAccruedCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponONCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponONDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of an instrument for a given YieldCurveBundle (set of yield curve that the instrument is sensitive to)
 * @deprecated Use the present values calculators that reference {@link ParameterProviderInterface}
 * e.g. {@link PresentValueDiscountingCalculator}
 */
@Deprecated
public class PresentValueCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The method unique instance.
   */
  private static final PresentValueCalculator INSTANCE = new PresentValueCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueCalculator() {
  }

  /**
   * The method used for different instruments.
   */
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositZeroDiscountingMethod METHOD_DEPOSIT_ZERO = DepositZeroDiscountingMethod.getInstance();
  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TRANSACTION = BillTransactionDiscountingMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final CouponONDiscountingMethod METHOD_CPN_OIS = CouponONDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  private static final CouponIborGearingDiscountingMethod METHOD_CPN_IBOR_GEARING = CouponIborGearingDiscountingMethod.getInstance();
  private static final CouponIborCompoundedDiscountingMethod METHOD_CPN_IBOR_COMP = CouponIborCompoundedDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingBundleMethod METHOD_FRA = ForwardRateAgreementDiscountingBundleMethod.getInstance();
  private static final CouponCMSDiscountingMethod METHOD_CMS_DISCOUNTING = CouponCMSDiscountingMethod.getInstance();
  private static final CouponFixedAccruedCompoundingDiscountingMethod METHOD_CPN_FIXED_ACCRUING = CouponFixedAccruedCompoundingDiscountingMethod.getInstance();
  private static final CouponONCompoundedDiscountingMethod METHOD_CPN_ON_COMPOUNDED = CouponONCompoundedDiscountingMethod.getInstance();

  // -----     Deposit     ------

  @Override
  public Double visitCash(final Cash deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT.presentValue(deposit, curves).getAmount();
  }

  @Override
  public Double visitDepositZero(final DepositZero deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT_ZERO.presentValue(deposit, curves).getAmount();
  }

  // -----     Bill/Bond     ------

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final YieldCurveBundle curves) {
    return METHOD_BILL_SECURITY.presentValue(bill, curves).getAmount();
  }

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final YieldCurveBundle curves) {
    return METHOD_BILL_TRANSACTION.presentValue(bill, curves).getAmount();
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(bond, "bond");
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(bond, "bond");
    final BondTransactionDiscountingMethod method = BondTransactionDiscountingMethod.getInstance();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondIborSecurity(final BondIborSecurity bond, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(bond, "bond");
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondIborTransaction(final BondIborTransaction bond, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(bond, "bond");
    final BondTransactionDiscountingMethod method = BondTransactionDiscountingMethod.getInstance();
    return method.presentValue(bond, curves);
  }

  // -----     Payment/Coupon     ------

  @Override
  public Double visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle curves) {
    return METHOD_PAY_FIXED.presentValue(payment, curves).getAmount();
  }

  @Override
  public Double visitCouponFixed(final CouponFixed payment, final YieldCurveBundle curves) {
    return METHOD_CPN_FIXED.presentValue(payment, curves).getAmount();
  }

  @Override
  public Double visitCouponOIS(final CouponON payment, final YieldCurveBundle data) {
    return METHOD_CPN_OIS.presentValue(payment, data).getAmount();
  }

  @Override
  public Double visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_IBOR.presentValue(coupon, curves).getAmount();
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment, final YieldCurveBundle curves) {
    return METHOD_CPN_IBOR_SPREAD.presentValue(payment, curves).getAmount();
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_IBOR_GEARING.presentValue(coupon, curves).getAmount();
  }

  @Override
  public Double visitCouponIborCompounding(final CouponIborCompounding coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_IBOR_COMP.presentValue(coupon, curves).getAmount();
  }

  @Override
  public Double visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_FIXED_ACCRUING.presentValue(coupon, curves).getAmount();
  }

  @Override
  public Double visitCouponONCompounded(final CouponONCompounded coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_ON_COMPOUNDED.presentValue(coupon, curves).getAmount();
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return METHOD_FRA.presentValue(fra, curves).getAmount();
  }

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(future, "future");
    final InterestRateFutureTransactionDiscountingMethod method = InterestRateFutureTransactionDiscountingMethod.getInstance();
    return method.presentValue(future, curves).getAmount();
  }

  @Override
  public Double visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(swap, "swap");
    final double pvFirst = swap.getFirstLeg().accept(this, curves);
    final double pvSecond = swap.getSecondLeg().accept(this, curves);
    return pvSecond + pvFirst;
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitBondFuture(final BondFuture bondFuture, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(bondFuture, "bond future");
    final BondFutureDiscountingMethod method = BondFutureDiscountingMethod.getInstance();
    return method.presentValue(bondFuture, curves).getAmount();
  }

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(annuity, "annuity");
    double pv = 0;
    for (final Payment p : annuity.getPayments()) {
      pv += p.accept(this, curves);
    }
    return pv;
  }

  @Override
  public Double visitForexForward(final ForexForward fx, final YieldCurveBundle data) {
    final double leg1 = visitFixedPayment(fx.getPaymentCurrency1(), data);
    final double leg2 = visitFixedPayment(fx.getPaymentCurrency2(), data);
    return leg1 + fx.getSpotForexRate() * leg2;
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public Double visitCouponCMS(final CouponCMS cmsCoupon, final YieldCurveBundle curves) {
    return METHOD_CMS_DISCOUNTING.presentValue(cmsCoupon, curves).getAmount();
  }

}
