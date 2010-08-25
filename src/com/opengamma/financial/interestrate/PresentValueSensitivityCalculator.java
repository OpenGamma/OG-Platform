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

/**
 * 
 */
public final class PresentValueSensitivityCalculator implements InterestRateDerivativeVisitor<Map<String, List<DoublesPair>>> {

  private static PresentValueSensitivityCalculator s_instance = new PresentValueSensitivityCalculator();

  public static PresentValueSensitivityCalculator getInstance() {
    return s_instance;
  }

  private PresentValueSensitivityCalculator() {
  }

  @Override
  public Map<String, List<DoublesPair>> getValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    return instrument.accept(this, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCash(final Cash cash, final YieldCurveBundle curves) {
    final String curveName = cash.getYieldCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = cash.getTradeTime();
    final double tb = cash.getPaymentTime();
    final DoublesPair s1 = new DoublesPair(ta, ta * curve.getDiscountFactor(ta));
    final DoublesPair s2 = new DoublesPair(tb, -tb * curve.getDiscountFactor(tb) * (1 + cash.getYearFraction() * cash.getRate()));
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final String fundingCurveName = fra.getFundingCurveName();
    final String liborCurveName = fra.getLiborCurveName();
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(fundingCurveName);
    final YieldAndDiscountCurve liborCurve = curves.getCurve(liborCurveName);
    final double fwdAlpha = fra.getForwardYearFraction();
    final double discountAlpha = fra.getDiscountingYearFraction();
    final double fixingDate = fra.getFixingDate();
    final double settlementDate = fra.getSettlementDate();
    final double maturity = fra.getMaturity();

    final double fwd = (liborCurve.getDiscountFactor(fra.getFixingDate()) / liborCurve.getDiscountFactor(fra.getMaturity()) - 1.0) / fwdAlpha;
    final double onePlusAlphaF = 1 + discountAlpha * fwd;

    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> temp = new ArrayList<DoublesPair>();
    if (settlementDate > 0) {
      final DoublesPair s = new DoublesPair(settlementDate, -settlementDate * fundingCurve.getDiscountFactor(settlementDate) * (fwd - fra.getStrike()) * fwdAlpha / onePlusAlphaF);
      temp.add(s);
      if (!fundingCurveName.equals(liborCurveName)) {
        result.put(fundingCurveName, temp);
        temp = new ArrayList<DoublesPair>();
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
  public Map<String, List<DoublesPair>> visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {

    final String curveName = future.getCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);

    final double ta = future.getFixingDate();
    final double tb = future.getMaturity();

    final double ratio = future.getValueYearFraction() / future.getIndexYearFraction() * curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb);
    final DoublesPair s1 = new DoublesPair(ta, ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, -tb * ratio);
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitBond(final Bond bond, final YieldCurveBundle curves) {
    return getValue(bond.getFixedAnnuity(), curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwap(final Swap swap, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> senseR = getValue(swap.getReceiveLeg(), curves);
    final Map<String, List<DoublesPair>> senseP = getValue(swap.getPayLeg(), curves);

    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : curves.getAllNames()) {
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      if (senseR.containsKey(name)) {
        for (final DoublesPair pair : senseR.get(name)) {
          temp.add(pair);
        }
      }
      if (senseP.containsKey(name)) {
        for (final DoublesPair pair : senseP.get(name)) {
          final DoublesPair newPair = new DoublesPair(pair.getFirst(), -pair.getSecond());
          temp.add(newPair);
        }
      }
      result.put(name, temp);
    }
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitBasisSwap(final BasisSwap swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedAnnuity(final FixedAnnuity annuity, final YieldCurveBundle curves) {
    final String curveName = annuity.getFundingCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double[] t = annuity.getPaymentTimes();
    final double[] c = annuity.getPaymentAmounts();
    final int n = annuity.getNumberOfPayments();
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    for (int i = 0; i < n; i++) {
      final DoublesPair s = new DoublesPair(t[i], -t[i] * c[i] * curve.getDiscountFactor(t[i]));
      temp.add(s);
    }
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitConstantCouponAnnuity(final ConstantCouponAnnuity annuity, final YieldCurveBundle curves) {
    return visitFixedAnnuity(annuity, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitVariableAnnuity(final VariableAnnuity annuity, final YieldCurveBundle curves) {
    final String fundingCurveName = annuity.getFundingCurveName();
    final String liborCurveName = annuity.getLiborCurveName();
    final YieldAndDiscountCurve fundCurve = curves.getCurve(fundingCurveName);
    final YieldAndDiscountCurve liborCurve = curves.getCurve(liborCurveName);
    final double notional = annuity.getNotional();
    final double[] libors = getLiborRates(annuity, curves);
    final double[] t = annuity.getPaymentTimes();
    final double[] spreads = annuity.getSpreads();
    final double[] yearFrac = annuity.getYearFractions();
    final double[] deltaStart = annuity.getDeltaStart();
    final double[] deltaEnd = annuity.getDeltaEnd();
    final int n = annuity.getNumberOfPayments();
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();

    List<DoublesPair> temp = new ArrayList<DoublesPair>();
    DoublesPair s;
    for (int i = 0; i < n; i++) {
      s = new DoublesPair(t[i], -t[i] * fundCurve.getDiscountFactor(t[i]) * (libors[i] + spreads[i]) * yearFrac[i] * notional);
      temp.add(s);
    }

    if (!liborCurveName.equals(fundingCurveName)) {
      result.put(fundingCurveName, temp);
      temp = new ArrayList<DoublesPair>();
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

  private double[] getLiborRates(final VariableAnnuity annuity, final YieldCurveBundle curves) {

    final YieldAndDiscountCurve curve = curves.getCurve(annuity.getLiborCurveName());
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
