/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, this calculates the sensitivity of the present value (PV) to points on the yield curve(s) (i.e. dPV/dR at every point the instrument has sensitivity). The return 
 * format is a map with curve names (String) as keys and List of DoublesPair as the values; each list holds set of time (corresponding to point of the yield curve) and sensitivity pairs 
 * (i.e. dPV/dR at that time). <b>Note:</b> The length of the list is instrument dependent and may have repeated times (with the understanding the sensitivities should be summed).
 */
public final class PresentValueSensitivityCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> {
  private static PresentValueSensitivityCalculator s_instance = new PresentValueSensitivityCalculator();

  public static PresentValueSensitivityCalculator getInstance() {
    return s_instance;
  }

  private PresentValueSensitivityCalculator() {
  }

  @Override
  public Map<String, List<DoublesPair>> visit(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    return instrument.accept(this, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCash(final Cash cash, final YieldCurveBundle curves) {
    final String curveName = cash.getYieldCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = cash.getTradeTime();
    final double tb = cash.getMaturity();
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
    final String liborCurveName = fra.getIndexCurveName();
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
    return visit(bond.getAnnuity(), curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> senseR = visit(swap.getReceiveLeg(), curves);
    final Map<String, List<DoublesPair>> senseP = visit(swap.getPayLeg(), curves);

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
  public Map<String, List<DoublesPair>> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitTenorSwap(final TenorSwap swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle data) {
    final Map<String, List<DoublesPair>> map = new HashMap<String, List<DoublesPair>>();
    for (final Payment p : annuity.getPayments()) {
      final Map<String, List<DoublesPair>> tempMap = visit(p, data);
      for (final String name : tempMap.keySet()) {
        if (!map.containsKey(name)) {
          map.put(name, tempMap.get(name));
        } else {
          final List<DoublesPair> tempList = map.get(name);
          tempList.addAll(tempMap.get(name));
          map.put(name, tempList);
        }
      }
    }
    return map;
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedPayment(final FixedPayment payment, final YieldCurveBundle data) {
    final String curveName = payment.getFundingCurveName();
    final YieldAndDiscountCurve curve = data.getCurve(curveName);
    final double t = payment.getPaymentTime();

    final DoublesPair s = new DoublesPair(t, -t * payment.getAmount() * curve.getDiscountFactor(t));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, list);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardLiborPayment(final ForwardLiborPayment payment, final YieldCurveBundle data) {
    final String fundingCurveName = payment.getFundingCurveName();
    final String liborCurveName = payment.getLiborCurveName();
    final YieldAndDiscountCurve fundCurve = data.getCurve(fundingCurveName);
    final YieldAndDiscountCurve liborCurve = data.getCurve(liborCurveName);

    final double tPay = payment.getPaymentTime();
    final double ta = payment.getLiborFixingTime();
    final double tb = payment.getLiborMaturityTime();
    final double dfPay = fundCurve.getDiscountFactor(tPay);
    final double dfa = liborCurve.getDiscountFactor(ta);
    final double dfb = liborCurve.getDiscountFactor(tb);
    final double forward = (dfa / dfb - 1) / payment.getForwardYearFraction();
    final double notional = payment.getNotional();

    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();

    List<DoublesPair> temp = new ArrayList<DoublesPair>();
    DoublesPair s;
    s = new DoublesPair(tPay, -tPay * dfPay * notional * (forward + payment.getSpread()) * payment.getPaymentYearFraction());
    temp.add(s);

    if (!liborCurveName.equals(fundingCurveName)) {
      result.put(fundingCurveName, temp);
      temp = new ArrayList<DoublesPair>();
    }

    final double ratio = notional * dfPay * dfa / dfb * payment.getPaymentYearFraction() / payment.getForwardYearFraction();
    s = new DoublesPair(ta, -ta * ratio);
    temp.add(s);
    s = new DoublesPair(tb, tb * ratio);
    temp.add(s);

    result.put(liborCurveName, temp);

    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment, final YieldCurveBundle data) {
    final YieldAndDiscountCurve fundingCurve = data.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve indexCurve = data.getCurve(payment.getIndexCurveName());
    final double ta = payment.getStartTime();
    final double tb = payment.getEndTime();
    final double tPay = payment.getPaymentTime();
    final double avRate = (indexCurve.getInterestRate(tb) * tb - indexCurve.getInterestRate(ta) * ta) / payment.getRateYearFraction();
    final double dfPay = fundingCurve.getDiscountFactor(tPay);
    final double notional = payment.getNotional();

    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    List<DoublesPair> temp = new ArrayList<DoublesPair>();
    DoublesPair s;
    s = new DoublesPair(tPay, -tPay * dfPay * notional * (avRate + payment.getSpread()) * payment.getPaymentYearFraction());
    temp.add(s);

    if (!payment.getIndexCurveName().equals(payment.getFundingCurveName())) {
      result.put(payment.getFundingCurveName(), temp);
      temp = new ArrayList<DoublesPair>();
    }

    final double ratio = notional * dfPay * payment.getPaymentYearFraction() / payment.getRateYearFraction();
    s = new DoublesPair(ta, -ta * ratio);
    temp.add(s);
    s = new DoublesPair(tb, tb * ratio);
    temp.add(s);

    result.put(payment.getIndexCurveName(), temp);

    return result;
  }

}
