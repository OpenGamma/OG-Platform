/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

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
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponOISDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;

/**
 * Calculates the present value of an instrument for a given YieldCurveBundle (set of yield curve that the instrument is sensitive to)
 */
public class PresentValueCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

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
  private static final CouponOISDiscountingMethod METHOD_CPN_OIS = CouponOISDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  private static final CouponIborGearingDiscountingMethod METHOD_CPN_IBOR_GEARING = CouponIborGearingDiscountingMethod.getInstance();
  private static final CouponIborCompoundedDiscountingMethod METHOD_CPN_IBOR_COMP = CouponIborCompoundedDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final CouponCMSDiscountingMethod METHOD_CMS_DISCOUNTING = CouponCMSDiscountingMethod.getInstance();

  @Override
  public Double visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double[] visit(final InstrumentDerivative[] derivative, final YieldCurveBundle curves) {
    Validate.notNull(derivative, "derivative");
    Validate.noNullElements(derivative, "derivative");
    Validate.notNull(curves, "curves");
    final Double[] output = new Double[derivative.length];
    for (int loopderivative = 0; loopderivative < derivative.length; loopderivative++) {
      output[loopderivative] = derivative[loopderivative].accept(this, curves);
    }
    return output;
  }

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
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondTransactionDiscountingMethod method = BondTransactionDiscountingMethod.getInstance();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondIborSecurity(final BondIborSecurity bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondIborTransaction(final BondIborTransaction bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
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
  public Double visitCouponOIS(final CouponOIS payment, final YieldCurveBundle data) {
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
  public Double visitCouponIborCompounded(final CouponIborCompounded coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_IBOR_COMP.presentValue(coupon, curves).getAmount();
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return METHOD_FRA.presentValue(fra, curves).getAmount();
  }

  /**
   * {@inheritDoc}
   * Future transaction pricing without convexity adjustment.
   */
  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(future);
    final InterestRateFutureDiscountingMethod method = InterestRateFutureDiscountingMethod.getInstance();
    return method.presentValue(future, curves).getAmount();
  }

  @Override
  public Double visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    final double pvFirst = visit(swap.getFirstLeg(), curves);
    final double pvSecond = visit(swap.getSecondLeg(), curves);
    return pvSecond + pvFirst;
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitBondFuture(final BondFuture bondFuture, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bondFuture);
    final BondFutureDiscountingMethod method = BondFutureDiscountingMethod.getInstance();
    return method.presentValue(bondFuture, curves).getAmount();
  }

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    double pv = 0;
    for (final Payment p : annuity.getPayments()) {
      pv += visit(p, curves);
    }
    return pv;
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
