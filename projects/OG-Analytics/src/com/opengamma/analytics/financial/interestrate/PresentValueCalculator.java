/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
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
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.method.BondFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.analytics.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponOISDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.analytics.financial.interestrate.swap.definition.ForexForward;
import com.opengamma.analytics.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.analytics.financial.interestrate.swap.definition.Swap;
import com.opengamma.analytics.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

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
  private static final CouponOISDiscountingMethod METHOD_OIS = new CouponOISDiscountingMethod();
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositZeroDiscountingMethod METHOD_DEPOSIT_ZERO = DepositZeroDiscountingMethod.getInstance();
  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TRANSACTION = BillTransactionDiscountingMethod.getInstance();
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

  @Override
  public Double visitCash(final Cash deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT.presentValue(deposit, curves).getAmount();
  }

  @Override
  public Double visitDepositZero(final DepositZero deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT_ZERO.presentValue(deposit, curves).getAmount();
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(fra);
    return ForwardRateAgreementDiscountingMethod.getInstance().presentValue(fra, curves).getAmount();
  }

  /**
   * {@inheritDoc}
   * Future transaction pricing without convexity adjustment.
   */
  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
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
  public Double visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitTenorSwap(final TenorSwap<? extends Payment> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitOISSwap(final OISSwap swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    return visitSwap(swap, curves);
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

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final YieldCurveBundle curves) {
    Validate.notNull(curves, "Curves");
    Validate.notNull(bill, "Bill");
    return METHOD_BILL_SECURITY.presentValue(bill, curves).getAmount();
  }

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final YieldCurveBundle curves) {
    Validate.notNull(curves, "Curves");
    Validate.notNull(bill, "Bill");
    return METHOD_BILL_TRANSACTION.presentValue(bill, curves).getAmount();
  }

  @Override
  public Double visitBondFuture(final BondFuture bondFuture, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bondFuture);
    final BondFutureDiscountingMethod method = BondFutureDiscountingMethod.getInstance();
    return method.presentValue(bondFuture, curves).getAmount();
  }

  @Override
  public Double visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    double pv = 0;
    for (final Payment p : annuity.getPayments()) {
      pv += visit(p, curves);
    }
    return pv;
  }

  @Override
  public Double visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(payment.getFundingCurveName());
    return payment.getAmount() * fundingCurve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve liborCurve = curves.getCurve(payment.getForwardCurveName());
    final double forward = (liborCurve.getDiscountFactor(payment.getFixingPeriodStartTime()) / liborCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1) / payment.getFixingYearFraction();
    return payment.getNotional() * (forward + payment.getSpread()) * payment.getPaymentYearFraction() * fundingCurve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponOIS(final CouponOIS payment, final YieldCurveBundle data) {
    return METHOD_OIS.presentValue(payment, data).getAmount();
  }

  @Override
  public Double visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle data) {
    return visitSwap(frn, data);
  }

  @Override
  public Double visitCrossCurrencySwap(final CrossCurrencySwap ccs, final YieldCurveBundle data) {
    double domesticValue = visit(ccs.getDomesticLeg(), data);
    double foreignValue = visit(ccs.getForeignLeg(), data);
    double fx = ccs.getSpotFX();
    return domesticValue - fx * foreignValue;
  }

  @Override
  public Double visitForexForward(final ForexForward fx, final YieldCurveBundle data) {
    double leg1 = visitFixedPayment(fx.getPaymentCurrency1(), data);
    double leg2 = visitFixedPayment(fx.getPaymentCurrency2(), data);
    return leg1 + fx.getSpotForexRate() * leg2;
  }

  @Override
  public Double visitForwardLiborAnnuity(final AnnuityCouponIbor annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public Double visitFixedCouponPayment(final CouponFixed payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(payment.getFundingCurveName());
    return payment.getAmount() * fundingCurve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public Double visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle curves) {
    return visitFixedCouponSwap(swap, curves);
  }

  @Override
  public Double visitCouponCMS(final CouponCMS cmsCoupon, final YieldCurveBundle curves) {
    return METHOD_CMS_DISCOUNTING.presentValue(cmsCoupon, curves).getAmount();
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    final CouponIborGearingDiscountingMethod method = CouponIborGearingDiscountingMethod.getInstance();
    return method.presentValue(coupon, curves).getAmount();
  }

  @Override
  public Double visitCouponIborFixed(final CouponIborFixed payment, final YieldCurveBundle data) {
    return visitCouponIbor(payment.toCouponIbor(), data);
  }
}
