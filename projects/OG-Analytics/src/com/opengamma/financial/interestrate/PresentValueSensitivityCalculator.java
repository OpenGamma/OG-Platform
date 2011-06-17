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

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.bond.method.BondTransactionDiscountingMethod;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureTransactionDiscountingMethod;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.method.CouponCMSDiscountingMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, this calculates the sensitivity of the present value (PV) to points on the yield curve(s) (i.e. dPV/dR at every point the instrument has sensitivity). The return 
 * format is a map with curve names (String) as keys and List of DoublesPair as the values; each list holds set of time (corresponding to point of the yield curve) and sensitivity pairs 
 * (i.e. dPV/dR at that time). <b>Note:</b> The length of the list is instrument dependent and may have repeated times (with the understanding the sensitivities should be summed).
 */
public class PresentValueSensitivityCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> {
  //TODO: Change the output format to PresentValueSensitivity.

  private static PresentValueSensitivityCalculator s_instance = new PresentValueSensitivityCalculator();

  public static PresentValueSensitivityCalculator getInstance() {
    return s_instance;
  }

  PresentValueSensitivityCalculator() {
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
  /**
   * Future transaction pricing without convexity adjustment.
   */
  public Map<String, List<DoublesPair>> visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    InterestRateFutureTransactionDiscountingMethod method = new InterestRateFutureTransactionDiscountingMethod();
    return method.presentValueCurveSensitivity(future, curves).getSensitivity();
  }

  @Override
  public Map<String, List<DoublesPair>> visitBond(final Bond bond, final YieldCurveBundle curves) {
    return visit(bond.getAnnuity(), curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitBondFixedTransaction(final BondFixedTransaction bond, final YieldCurveBundle curves) {
    BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    return method.presentValueSensitivity(bond, curves).getSensitivity();
  }

  @Override
  public Map<String, List<DoublesPair>> visitBondIborTransaction(final BondIborTransaction bond, final YieldCurveBundle curves) {
    BondTransactionDiscountingMethod method = new BondTransactionDiscountingMethod();
    return method.presentValueSensitivity(bond, curves).getSensitivity();
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> senseR = visit(swap.getSecondLeg(), curves);
    final Map<String, List<DoublesPair>> senseP = visit(swap.getFirstLeg(), curves);

    return PresentValueSensitivityUtil.addSensitivity(curves, senseR, senseP);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitTenorSwap(final TenorSwap<? extends Payment> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  //  @Override
  //  public Map<String, List<DoublesPair>> visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
  //    return visitSwap(frn, curves);
  //  }

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
  public Map<String, List<DoublesPair>> visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle data) {
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
  public Map<String, List<DoublesPair>> visitCouponIbor(final CouponIbor payment, final YieldCurveBundle data) {
    final String fundingCurveName = payment.getFundingCurveName();
    final String liborCurveName = payment.getForwardCurveName();
    final YieldAndDiscountCurve fundCurve = data.getCurve(fundingCurveName);
    final YieldAndDiscountCurve liborCurve = data.getCurve(liborCurveName);

    final double tPay = payment.getPaymentTime();
    //    final double tStart = payment.getFixingTime();
    final double tStart = payment.getFixingPeriodStartTime();
    final double tEnd = payment.getFixingPeriodEndTime();
    final double dfPay = fundCurve.getDiscountFactor(tPay);
    final double dfStart = liborCurve.getDiscountFactor(tStart);
    final double dfEnd = liborCurve.getDiscountFactor(tEnd);
    final double forward = (dfStart / dfEnd - 1) / payment.getFixingYearFraction();
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

    final double ratio = notional * dfPay * dfStart / dfEnd * payment.getPaymentYearFraction() / payment.getFixingYearFraction();
    s = new DoublesPair(tStart, -tStart * ratio);
    temp.add(s);
    s = new DoublesPair(tEnd, tEnd * ratio);
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

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponPayment(final CouponFixed payment, final YieldCurveBundle data) {
    return visitFixedPayment(payment, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardLiborAnnuity(final AnnuityCouponIbor annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle data) {
    return visitFixedCouponSwap(swap, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponCMS(CouponCMS payment, YieldCurveBundle data) {
    CouponCMSDiscountingMethod method = new CouponCMSDiscountingMethod();
    return method.presentValueSensitivity(payment, data).getSensitivity();
  }

  /**
   * Compute the sensitivity of the discount factor at a given time.
   * @param curveName The curve name associated to the discount factor.
   * @param curve The curve from which the discount factor should be computed.
   * @param time The time
   * @return The sensitivity.
   */
  public static Map<String, List<DoublesPair>> discountFactorSensitivity(String curveName, YieldAndDiscountCurve curve, double time) {
    final DoublesPair s = new DoublesPair(time, -time * curve.getDiscountFactor(time));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, list);
    return result;
  }

}
