/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.financial.interestrate.InterestRateCurveSensitivityUtils.addSensitivity;
import static com.opengamma.financial.interestrate.InterestRateCurveSensitivityUtils.multiplySensitivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.financial.interestrate.bond.method.BondTransactionDiscountingMethod;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.method.BondFutureDiscountingMethod;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.interestrate.payments.method.CouponCMSDiscountingMethod;
import com.opengamma.financial.interestrate.payments.method.CouponIborGearingDiscountingMethod;
import com.opengamma.financial.interestrate.payments.method.CouponOISDiscountingMethod;
import com.opengamma.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.ForexForward;
import com.opengamma.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, this calculates the sensitivity of the present value (PV) to points on the yield curve(s) (i.e. dPV/dR at every point the instrument has sensitivity). The return 
 * format is a map with curve names (String) as keys and List of DoublesPair as the values; each list holds set of time (corresponding to point of the yield curve) and sensitivity pairs 
 * (i.e. dPV/dR at that time). <b>Note:</b> The length of the list is instrument dependent and may have repeated times (with the understanding the sensitivities should be summed).
 */
public class PresentValueCurveSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> {
  //TODO: Change the output format to PresentValueSensitivity.
  //TODO Rename me to be consistent with descendants and the par rate calculators

  /**
   * The method used for OIS coupons.
   */
  private static final CouponOISDiscountingMethod METHOD_OIS = new CouponOISDiscountingMethod();

  private static PresentValueCurveSensitivityCalculator s_instance = new PresentValueCurveSensitivityCalculator();

  public static PresentValueCurveSensitivityCalculator getInstance() {
    return s_instance;
  }

  PresentValueCurveSensitivityCalculator() {
  }

  @Override
  public Map<String, List<DoublesPair>> visit(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return instrument.accept(this, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCash(final Cash cash, final YieldCurveBundle curves) {
    final String curveName = cash.getYieldCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = cash.getTradeTime();
    final double tb = cash.getMaturity();
    final DoublesPair s1 = new DoublesPair(ta, cash.getNotional() * ta * curve.getDiscountFactor(ta));
    final DoublesPair s2 = new DoublesPair(tb, -cash.getNotional() * tb * curve.getDiscountFactor(tb) * (1 + cash.getYearFraction() * cash.getRate()));
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final ForwardRateAgreementDiscountingMethod method = ForwardRateAgreementDiscountingMethod.getInstance();
    return method.presentValueCurveSensitivity(fra, curves).getSensitivities();
  }

  /**
   * {@inheritDoc}
   * Future transaction pricing without convexity adjustment.
   */
  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    InterestRateFutureDiscountingMethod method = InterestRateFutureDiscountingMethod.getInstance();
    return method.presentValueCurveSensitivity(future, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.presentValueSensitivity(bond, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitBondFixedTransaction(final BondFixedTransaction bond, final YieldCurveBundle curves) {
    final BondTransactionDiscountingMethod method = BondTransactionDiscountingMethod.getInstance();
    return method.presentValueSensitivity(bond, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitBondIborTransaction(final BondIborTransaction bond, final YieldCurveBundle curves) {
    final BondTransactionDiscountingMethod method = BondTransactionDiscountingMethod.getInstance();
    return method.presentValueSensitivity(bond, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitBondFuture(final BondFuture bondFuture, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bondFuture);
    final BondFutureDiscountingMethod method = BondFutureDiscountingMethod.getInstance();
    return method.presentValueCurveSensitivity(bondFuture, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> senseR = visit(swap.getSecondLeg(), curves);
    final Map<String, List<DoublesPair>> senseP = visit(swap.getFirstLeg(), curves);
    return addSensitivity(senseR, senseP);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitOISSwap(final OISSwap swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitTenorSwap(final TenorSwap<? extends Payment> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCrossCurrencySwap(final CrossCurrencySwap ccs, final YieldCurveBundle curves) {
    Map<String, List<DoublesPair>> senseD = visit(ccs.getDomesticLeg(), curves);
    Map<String, List<DoublesPair>> senseF = visit(ccs.getForeignLeg(), curves);
    //Note the sensitivities subtract rather than add here because the CCS is set up as domestic FRN minus a foreign FRN
    return addSensitivity(senseD, multiplySensitivity(senseF, -ccs.getSpotFX()));
  }

  @Override
  public Map<String, List<DoublesPair>> visitForexForward(final ForexForward fx, final YieldCurveBundle curves) {
    Map<String, List<DoublesPair>> senseP1 = visit(fx.getPaymentCurrency1(), curves);
    Map<String, List<DoublesPair>> senseP2 = visit(fx.getPaymentCurrency2(), curves);
    //Note the sensitivities add rather than subtract here because the FX Forward is set up as a notional in one currency PLUS a notional in another  with the  opposite sign
    return InterestRateCurveSensitivityUtils.addSensitivity(senseP1, multiplySensitivity(senseP2, fx.getSpotForexRate()));
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
  public Map<String, List<DoublesPair>> visitCouponOIS(final CouponOIS payment, final YieldCurveBundle data) {

    Map<String, List<DoublesPair>> result = METHOD_OIS.presentValueCurveSensitivity(payment, data).getSensitivities();

    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponPayment(final CouponFixed payment, final YieldCurveBundle data) {
    return visitFixedPayment(payment.toPaymentFixed(), data);
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
  public Map<String, List<DoublesPair>> visitCouponCMS(final CouponCMS payment, final YieldCurveBundle data) {
    final CouponCMSDiscountingMethod method = CouponCMSDiscountingMethod.getInstance();
    return method.presentValueSensitivity(payment, data).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIborGearing(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    final CouponIborGearingDiscountingMethod method = CouponIborGearingDiscountingMethod.getInstance();
    return method.presentValueCurveSensitivity(coupon, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIborFixed(final CouponIborFixed payment, final YieldCurveBundle data) {
    return visitCouponIbor(payment.toCouponIbor(), data);
  }

  /**
   * Compute the sensitivity of the discount factor at a given time.
   * @param curveName The curve name associated to the discount factor.
   * @param curve The curve from which the discount factor should be computed.
   * @param time The time
   * @return The sensitivity.
   */
  public static Map<String, List<DoublesPair>> discountFactorSensitivity(final String curveName, final YieldAndDiscountCurve curve, final double time) {
    final DoublesPair s = new DoublesPair(time, -time * curve.getDiscountFactor(time));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, list);
    return result;
  }

}
