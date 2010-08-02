/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class PresentValueCalculator implements InterestRateDerivativeVisitor<Double> {

  public double getPresentValue(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double visitSwap(Swap swap, YieldCurveBundle curves) {
    double pvPay = getPresentValue(swap.getPayLeg(), curves);
    double pvReceive = getPresentValue(swap.getReceiveLeg(), curves);
    return pvReceive - pvPay;
  }

  @Override
  public Double visitFixedFloatSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Double visitBond(Bond bond, YieldCurveBundle curves) {
    return getPresentValue(bond.getFixedAnnuity(), curves);
  }

  @Override
  public Double visitCash(Cash cash, YieldCurveBundle curves) {
    double ta = cash.getTradeTime();
    double tb = cash.getPaymentTime();
    YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    return curve.getDiscountFactor(tb) * (1 + cash.getYearFraction() * cash.getRate()) - curve.getDiscountFactor(ta);
  }

  @Override
  public Double visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    YieldAndDiscountCurve fundingCurve = curves.getCurve(fra.getFundingCurveName());
    YieldAndDiscountCurve liborCurve = curves.getCurve(fra.getLiborCurveName());
    double fwdAlpha = fra.getForwardYearFraction();
    double discountAlpha = fra.getDiscountingYearFraction();
    double forward = (liborCurve.getDiscountFactor(fra.getFixingDate()) / liborCurve.getDiscountFactor(fra.getMaturity()) - 1.0) / fwdAlpha;
    double fv = (forward - fra.getStrike()) * fwdAlpha / (1 + forward * discountAlpha);
    return fv * fundingCurve.getDiscountFactor(fra.getSettlementDate());
  }

  @Override
  public Double visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
    YieldAndDiscountCurve liborCurve = curves.getCurve(future.getCurveName());
    double ta = future.getSettlementDate();
    double alpha = future.getYearFraction();
    double tb = ta + alpha;
    double rate = (liborCurve.getDiscountFactor(ta) / liborCurve.getDiscountFactor(tb) - 1.0) / alpha;
    return alpha * (100 * (1 - rate) - future.getPrice());
  }

  @Override
  public Double visitFixedAnnuity(FixedAnnuity annuity, YieldCurveBundle curves) {
    Validate.notNull(annuity);
    Validate.notNull(curves);
    YieldAndDiscountCurve curve = curves.getCurve(annuity.getFundingCurveName());
    double[] t = annuity.getPaymentTimes();
    double[] c = annuity.getPaymentAmounts();
    int n = annuity.getNumberOfPayments();
    double res = 0;
    for (int i = 0; i < n; i++) {
      res += c[i] * curve.getDiscountFactor(t[i]);
    }
    return res;
  }

  @Override
  public Double visitVariableAnnuity(VariableAnnuity annuity, YieldCurveBundle curves) {

    YieldAndDiscountCurve fundCurve = curves.getCurve(annuity.getFundingCurveName());
    double[] libors = getLiborRates(annuity, curves);
    double[] t = annuity.getPaymentTimes();
    double[] spreads = annuity.getSpreads();
    final double[] alpha = annuity.getYearFractions();
    int n = annuity.getNumberOfPayments();
    double res = 0;
    for (int i = 0; i < n; i++) {
      res += (libors[i] + spreads[i]) * alpha[i] * fundCurve.getDiscountFactor(t[i]);
    }
    return res * annuity.getNotional();
  }

  private double[] getLiborRates(final VariableAnnuity annuity, YieldCurveBundle curves) {

    YieldAndDiscountCurve curve = curves.getCurve(annuity.getLiborCurveName());
    final int n = annuity.getNumberOfPayments();
    final double[] paymentTimes = annuity.getPaymentTimes();
    final double[] deltaStart = annuity.getDeltaStart();
    final double[] deltaEnd = annuity.getDeltaEnd();
    final double[] alpha = annuity.getYearFractions();
    final double[] libors = new double[n];
    double ta, tb;
    for (int i = 0; i < n; i++) {
      ta = (i == 0 ? 0.0 : paymentTimes[i - 1]) + deltaStart[i];
      tb = paymentTimes[i] + deltaEnd[i];
      libors[i] = (curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) - 1.0) / alpha[i];
    }
    return libors;
  }

}
