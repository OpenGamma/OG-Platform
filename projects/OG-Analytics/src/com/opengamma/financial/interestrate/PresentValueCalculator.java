/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public final class PresentValueCalculator implements InterestRateDerivativeVisitor<YieldCurveBundle, Double> {

  private static final PresentValueCalculator s_instance = new PresentValueCalculator();

  public static PresentValueCalculator getInstance() {
    return s_instance;
  }

  private PresentValueCalculator() {
  }

  @Override
  public Double getValue(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    final double ta = cash.getTradeTime();
    final double tb = cash.getMaturity();
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    return curve.getDiscountFactor(tb) * (1 + cash.getYearFraction() * cash.getRate()) - curve.getDiscountFactor(ta);
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve liborCurve = curves.getCurve(fra.getLiborCurveName());
    final double fwdAlpha = fra.getForwardYearFraction();
    final double discountAlpha = fra.getDiscountingYearFraction();
    final double forward = (liborCurve.getDiscountFactor(fra.getFixingDate()) / liborCurve.getDiscountFactor(fra.getMaturity()) - 1.0) / fwdAlpha;
    final double fv = (forward - fra.getStrike()) * fwdAlpha / (1 + forward * discountAlpha);
    return fv * fundingCurve.getDiscountFactor(fra.getSettlementDate());
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve liborCurve = curves.getCurve(future.getCurveName());
    final double ta = future.getFixingDate();
    final double tb = future.getMaturity();
    final double rate = (liborCurve.getDiscountFactor(ta) / liborCurve.getDiscountFactor(tb) - 1.0) / future.getIndexYearFraction();
    return future.getValueYearFraction() * (1 - rate - future.getPrice() / 100);
  }

  @Override
  public Double visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    final double pvPay = getValue(swap.getPayLeg(), curves);
    final double pvReceive = getValue(swap.getReceiveLeg(), curves);
    return pvReceive - pvPay;
  }

  @Override
  public Double visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitTenorSwap(final TenorSwap swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  @Override
  public Double visitBond(final Bond bond, final YieldCurveBundle curves) {
    return getValue(bond.getAnnuity(), curves);
  }

  @Override
  public Double visitGenericAnnuity(GenericAnnuity<? extends Payment> annuity, YieldCurveBundle data) {
    double pv = 0;
    for (Payment p : annuity.getPayments()) {
      pv += getValue(p, data);
    }
    return pv;
  }

  @Override
  public Double visitFixedPayment(FixedPayment payment, YieldCurveBundle data) {
    YieldAndDiscountCurve fundingCurve = data.getCurve(payment.getFundingCurveName());
    return payment.getAmount() * fundingCurve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitForwardLiborPayment(ForwardLiborPayment payment, YieldCurveBundle data) {
    final YieldAndDiscountCurve fundingCurve = data.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve liborCurve = data.getCurve(payment.getLiborCurveName());
    double forward = (liborCurve.getDiscountFactor(payment.getLiborFixingTime()) / liborCurve.getDiscountFactor(payment.getLiborMaturityTime()) - 1) / payment.getForwardYearFraction();
    return payment.getNotional() * (forward + payment.getSpread()) * payment.getPaymentYearFraction() * fundingCurve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment, YieldCurveBundle data) {
    final YieldAndDiscountCurve fundingCurve = data.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve indexCurve = data.getCurve(payment.getIndexCurveName());
    final double ta = payment.getStartTime();
    final double tb = payment.getEndTime();
    final double avRate = (indexCurve.getInterestRate(tb) * tb - indexCurve.getInterestRate(ta) * ta) / payment.getRateYearFraction();
    return fundingCurve.getDiscountFactor(payment.getPaymentTime()) * (avRate + payment.getSpread()) * payment.getPaymentYearFraction() * payment.getNotional();
  }

  // @Override
  // public Double visitFixedAnnuity(final FixedAnnuity annuity, final YieldCurveBundle curves) {
  // Validate.notNull(annuity);
  // Validate.notNull(curves);
  // final YieldAndDiscountCurve curve = curves.getCurve(annuity.getFundingCurveName());
  // final double[] t = annuity.getPaymentTimes();
  // final double[] c = annuity.getPaymentAmounts();
  // final int n = annuity.getNumberOfPayments();
  // double res = 0;
  // for (int i = 0; i < n; i++) {
  // res += c[i] * curve.getDiscountFactor(t[i]);
  // }
  // return res;
  // }
  //
  // @Override
  // public Double visitConstantCouponAnnuity(final FixedCouponAnnuity annuity, final YieldCurveBundle curves) {
  // return visitFixedAnnuity(annuity, curves);
  // }
  //
  // @Override
  // public Double visitVariableAnnuity(final ForwardLiborAnnuity annuity, final YieldCurveBundle curves) {
  //
  // final YieldAndDiscountCurve fundCurve = curves.getCurve(annuity.getFundingCurveName());
  // final double[] libors = AnnuityCalculations.getLiborRates(annuity, curves);
  // final double[] t = annuity.getPaymentTimes();
  // final double[] spreads = annuity.getSpreads();
  // final double[] alpha = annuity.getYearFractions();
  // final int n = annuity.getNumberOfPayments();
  // double res = 0;
  // for (int i = 0; i < n; i++) {
  // res += (libors[i] + spreads[i]) * alpha[i] * fundCurve.getDiscountFactor(t[i]);
  // }
  // return res * annuity.getNotional();
  // }

}
