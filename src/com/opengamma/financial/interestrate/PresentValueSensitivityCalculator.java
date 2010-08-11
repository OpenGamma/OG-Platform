/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public final class PresentValueSensitivityCalculator implements InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> {

  private static PresentValueSensitivityCalculator s_instance;

  public static PresentValueSensitivityCalculator getInstance() {
    if (s_instance == null) {
      s_instance = new PresentValueSensitivityCalculator();
    }
    return s_instance;
  }

  private PresentValueSensitivityCalculator() {
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> getValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    return instrument.accept(this, curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitCash(Cash cash, YieldCurveBundle curves) {
    String curveName = cash.getYieldCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);
    double ta = cash.getTradeTime();
    double tb = cash.getPaymentTime();
    final DoublesPair s1 = new DoublesPair(ta, ta * curve.getDiscountFactor(ta));
    final DoublesPair s2 = new DoublesPair(tb, -tb * curve.getDiscountFactor(tb) * (1 + cash.getYearFraction() * cash.getRate()));
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    temp.add(s1);
    temp.add(s2);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    String fundingCurveName = fra.getFundingCurveName();
    String liborCurveName = fra.getLiborCurveName();
    YieldAndDiscountCurve fundingCurve = curves.getCurve(fundingCurveName);
    YieldAndDiscountCurve liborCurve = curves.getCurve(liborCurveName);
    double fwdAlpha = fra.getForwardYearFraction();
    double discountAlpha = fra.getDiscountingYearFraction();
    double fixingDate = fra.getFixingDate();
    double settlementDate = fra.getSettlementDate();
    double maturity = fra.getMaturity();

    double fwd = (liborCurve.getDiscountFactor(fra.getFixingDate()) / liborCurve.getDiscountFactor(fra.getMaturity()) - 1.0) / fwdAlpha;
    double onePlusAlphaF = 1 + discountAlpha * fwd;

    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    if (settlementDate > 0) {
      final DoublesPair s = new DoublesPair(settlementDate, -settlementDate * fundingCurve.getDiscountFactor(settlementDate) * (fwd - fra.getStrike()) * fwdAlpha / onePlusAlphaF);
      temp.add(s);
      if (fundingCurveName != liborCurveName) {
        result.put(fundingCurveName, temp);
        temp = new ArrayList<Pair<Double, Double>>();
      }
    }

    double factor = fundingCurve.getDiscountFactor(settlementDate) * liborCurve.getDiscountFactor(fixingDate) / liborCurve.getDiscountFactor(maturity) / onePlusAlphaF;
    factor *= 1 - (fwd - fra.getStrike()) * discountAlpha / onePlusAlphaF;
    final DoublesPair s1 = new DoublesPair(fixingDate, -fixingDate * factor);
    final DoublesPair s2 = new DoublesPair(maturity, maturity * factor);
    temp.add(s1);
    temp.add(s2);
    result.put(liborCurveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {

    String curveName = future.getCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);

    final double ta = future.getSettlementDate();
    final double tb = ta + future.getYearFraction();

    final double ratio = 100 * curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb);
    final DoublesPair s1 = new DoublesPair(ta, ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, -tb * ratio);
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    temp.add(s1);
    temp.add(s2);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitBond(Bond bond, YieldCurveBundle curves) {
    return getValue(bond.getFixedAnnuity(), curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitSwap(Swap swap, YieldCurveBundle curves) {
    Map<String, List<Pair<Double, Double>>> senseR = getValue(swap.getReceiveLeg(), curves);
    Map<String, List<Pair<Double, Double>>> senseP = getValue(swap.getPayLeg(), curves);

    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    for (String name : curves.getAllNames()) {
      List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
      if (senseR.containsKey(name)) {
        for (Pair<Double, Double> pair : senseR.get(name)) {
          temp.add(pair);
        }
      }
      if (senseP.containsKey(name)) {
        for (Pair<Double, Double> pair : senseP.get(name)) {
          DoublesPair newPair = new DoublesPair(pair.getFirst(), -pair.getSecond());
          temp.add(newPair);
        }
      }
      result.put(name, temp);
    }
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitFixedFloatSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitFloatingRateNote(FloatingRateNote frn, YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitFixedAnnuity(FixedAnnuity annuity, YieldCurveBundle curves) {
    String curveName = annuity.getFundingCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);
    double[] t = annuity.getPaymentTimes();
    double[] c = annuity.getPaymentAmounts();
    int n = annuity.getNumberOfPayments();
    List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    for (int i = 0; i < n; i++) {
      DoublesPair s = new DoublesPair(t[i], -t[i] * c[i] * curve.getDiscountFactor(t[i]));
      temp.add(s);
    }
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitConstantCouponAnnuity(ConstantCouponAnnuity annuity, YieldCurveBundle curves) {
    return visitFixedAnnuity(annuity, curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitVariableAnnuity(VariableAnnuity annuity, YieldCurveBundle curves) {
    String fundingCurveName = annuity.getFundingCurveName();
    String liborCurveName = annuity.getLiborCurveName();
    YieldAndDiscountCurve fundCurve = curves.getCurve(fundingCurveName);
    YieldAndDiscountCurve liborCurve = curves.getCurve(liborCurveName);
    double notional = annuity.getNotional();
    double[] libors = getLiborRates(annuity, curves);
    double[] t = annuity.getPaymentTimes();
    double[] spreads = annuity.getSpreads();
    double[] yearFrac = annuity.getYearFractions();
    double[] deltaStart = annuity.getDeltaStart();
    double[] deltaEnd = annuity.getDeltaEnd();
    int n = annuity.getNumberOfPayments();
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();

    List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    DoublesPair s;
    for (int i = 0; i < n; i++) {
      s = new DoublesPair(t[i], -t[i] * fundCurve.getDiscountFactor(t[i]) * (libors[i] + spreads[i]) * yearFrac[i] * notional);
      temp.add(s);
    }

    if (liborCurveName != fundingCurveName) {
      result.put(fundingCurveName, temp);
      temp = new ArrayList<Pair<Double, Double>>();
    }

    double ta, tb, df, dfa, dfb, ratio;
    for (int i = 0; i < n; i++) {
      ta = (i == 0 ? 0.0 : t[i - 1]) + deltaStart[i];
      tb = t[i] + deltaEnd[i];
      df = fundCurve.getDiscountFactor(t[i]);
      dfa = liborCurve.getDiscountFactor(ta);
      dfb = liborCurve.getDiscountFactor(tb);
      ratio = notional * df * dfa / dfb;
      s = new DoublesPair(ta, -ta * ratio);
      temp.add(s);
      s = new DoublesPair(tb, tb * ratio);
      temp.add(s);

    }
    result.put(liborCurveName, temp);

    return result;
  }

  private double[] getLiborRates(final VariableAnnuity annuity, YieldCurveBundle curves) {

    YieldAndDiscountCurve curve = curves.getCurve(annuity.getLiborCurveName());
    final int n = annuity.getNumberOfPayments();
    final double[] paymentTimes = annuity.getPaymentTimes();
    final double[] deltaStart = annuity.getDeltaStart();
    final double[] deltaEnd = annuity.getDeltaEnd();
    final double[] yearFrac = annuity.getYearFractions();
    final double[] libors = new double[n];
    double ta, tb;
    for (int i = 0; i < n; i++) {
      ta = (i == 0 ? 0.0 : paymentTimes[i - 1]) + deltaStart[i];
      tb = paymentTimes[i] + deltaEnd[i];
      libors[i] = (curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) - 1.0) / yearFrac[i];
    }
    return libors;
  }

}
