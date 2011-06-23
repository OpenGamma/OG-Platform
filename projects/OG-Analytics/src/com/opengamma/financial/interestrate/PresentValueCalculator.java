/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.financial.interestrate.bond.method.BondTransactionDiscountingMethod;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.definition.ZZZForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureTransactionDiscountingMethod;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.method.CouponCMSDiscountingMethod;
import com.opengamma.financial.interestrate.payments.method.CouponIborGearingDiscountingMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Calculates the present value of an instrument for a given YieldCurveBundle (set of yield curve that the instrument is sensitive to) 
 */
public class PresentValueCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Double> {

  private static final PresentValueCalculator s_instance = new PresentValueCalculator();

  public static PresentValueCalculator getInstance() {
    return s_instance;
  }

  PresentValueCalculator() {
  }

  @Override
  public Double visit(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double[] visit(final InterestRateDerivative[] derivative, final YieldCurveBundle curves) {
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
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(cash);
    final double ta = cash.getTradeTime();
    final double tb = cash.getMaturity();
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    return curve.getDiscountFactor(tb) * (1 + cash.getYearFraction() * cash.getRate()) - curve.getDiscountFactor(ta);
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(fra);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve liborCurve = curves.getCurve(fra.getIndexCurveName());
    final double fwdAlpha = fra.getForwardYearFraction();
    final double discountAlpha = fra.getDiscountingYearFraction();
    final double forward = (liborCurve.getDiscountFactor(fra.getFixingDate()) / liborCurve.getDiscountFactor(fra.getMaturity()) - 1.0) / fwdAlpha;
    final double fv = (forward - fra.getStrike()) * fwdAlpha / (1 + forward * discountAlpha);
    return fv * fundingCurve.getDiscountFactor(fra.getSettlementDate());
  }

  @Override
  public Double visitZZZForwardRateAgreement(final ZZZForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(fra);
    final ForwardRateAgreementDiscountingMethod method = new ForwardRateAgreementDiscountingMethod();
    return method.presentValue(fra, curves).getAmount();
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(future);
    final YieldAndDiscountCurve liborCurve = curves.getCurve(future.getCurveName());
    final double ta = future.getFixingDate();
    final double tb = future.getMaturity();
    final double rate = (liborCurve.getDiscountFactor(ta) / liborCurve.getDiscountFactor(tb) - 1.0) / future.getIndexYearFraction();
    return future.getValueYearFraction() * (1 - rate - future.getPrice() / 100);
  }

  @Override
  /**
   * Future transaction pricing without convexity adjustment.
   */
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    final InterestRateFutureTransactionDiscountingMethod method = new InterestRateFutureTransactionDiscountingMethod();
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
  public Double visitBond(final Bond bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    return visit(bond.getAnnuity(), curves);
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondSecurityDiscountingMethod method = new BondSecurityDiscountingMethod();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondIborSecurity(final BondIborSecurity bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondSecurityDiscountingMethod method = new BondSecurityDiscountingMethod();
    return method.presentValue(bond, curves);
  }

  @Override
  public Double visitBondIborTransaction(final BondIborTransaction bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    return method.presentValue(bond, curves);
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
  public Double visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve indexCurve = curves.getCurve(payment.getIndexCurveName());
    final double ta = payment.getStartTime();
    final double tb = payment.getEndTime();
    final double avRate = (indexCurve.getInterestRate(tb) * tb - indexCurve.getInterestRate(ta) * ta) / payment.getRateYearFraction();
    return fundingCurve.getDiscountFactor(payment.getPaymentTime()) * (avRate + payment.getSpread()) * payment.getPaymentYearFraction() * payment.getNotional();
  }

  @Override
  public Double visitForwardLiborAnnuity(final AnnuityCouponIbor annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public Double visitFixedCouponPayment(final CouponFixed payment, final YieldCurveBundle curves) {
    return visitFixedPayment(payment, curves);
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
    final CouponCMSDiscountingMethod method = new CouponCMSDiscountingMethod();
    return method.presentValue(cmsCoupon, curves);
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    final CouponIborGearingDiscountingMethod method = new CouponIborGearingDiscountingMethod();
    return method.presentValue(coupon, curves).getAmount();
  }

}
