/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.addSensitivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, this calculates the sensitivity of the present value (PV) to points on the yield curve(s) (i.e. dPV/dR at every point the instrument has sensitivity). The return
 * format is a map with curve names (String) as keys and List of DoublesPair as the values; each list holds set of time (corresponding to point of the yield curve) and sensitivity pairs
 * (i.e. dPV/dR at that time). <b>Note:</b> The length of the list is instrument dependent and may have repeated times (with the understanding the sensitivities should be summed).
 */
public class PresentValueCurveSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> {
  //TODO: Change the output format from Map to InterestRateCurveSensitivity, which wraps the map and adds common functionality.

  /**
   * The method unique instance.
   */
  private static final PresentValueCurveSensitivityCalculator INSTANCE = new PresentValueCurveSensitivityCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueCurveSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityCalculator() {
  }

  /**
   * The methods.
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

  @Override
  public Map<String, List<DoublesPair>> visit(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return instrument.accept(this, curves);
  }

  // -----     Deposit     ------

  @Override
  public Map<String, List<DoublesPair>> visitCash(final Cash cash, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT.presentValueCurveSensitivity(cash, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitDepositZero(final DepositZero deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT_ZERO.presentValueCurveSensitivity(deposit, curves).getSensitivities();
  }

  // -----     Payment/Coupon     ------

  @Override
  public Map<String, List<DoublesPair>> visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle data) {
    return METHOD_PAY_FIXED.presentValueCurveSensitivity(payment, data).getSensitivities();
    //    final String curveName = payment.getFundingCurveName();
    //    final YieldAndDiscountCurve curve = data.getCurve(curveName);
    //    final double t = payment.getPaymentTime();
    //
    //    final DoublesPair s = new DoublesPair(t, -t * payment.getAmount() * curve.getDiscountFactor(t));
    //    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    //    list.add(s);
    //    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    //    result.put(curveName, list);
    //    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponFixed(final CouponFixed payment, final YieldCurveBundle data) {
    return METHOD_CPN_FIXED.presentValueCurveSensitivity(payment, data).getSensitivities();
    //    return visitFixedPayment(payment.toPaymentFixed(), data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIborSpread(final CouponIborSpread payment, final YieldCurveBundle data) {
    //    final String fundingCurveName = payment.getFundingCurveName();
    //    final String liborCurveName = payment.getForwardCurveName();
    //    final YieldAndDiscountCurve fundCurve = data.getCurve(fundingCurveName);
    //    final YieldAndDiscountCurve liborCurve = data.getCurve(liborCurveName);
    //
    //    final double tPay = payment.getPaymentTime();
    //    final double tStart = payment.getFixingPeriodStartTime();
    //    final double tEnd = payment.getFixingPeriodEndTime();
    //    final double dfPay = fundCurve.getDiscountFactor(tPay);
    //    final double dfStart = liborCurve.getDiscountFactor(tStart);
    //    final double dfEnd = liborCurve.getDiscountFactor(tEnd);
    //    final double forward = (dfStart / dfEnd - 1) / payment.getFixingYearFraction();
    //    final double notional = payment.getNotional();
    //
    //    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    //
    //    List<DoublesPair> temp = new ArrayList<DoublesPair>();
    //    DoublesPair s;
    //    s = new DoublesPair(tPay, -tPay * dfPay * notional * (forward + payment.getSpread()) * payment.getPaymentYearFraction());
    //    temp.add(s);
    //
    //    if (!liborCurveName.equals(fundingCurveName)) {
    //      result.put(fundingCurveName, temp);
    //      temp = new ArrayList<DoublesPair>();
    //    }
    //
    //    final double ratio = notional * dfPay * dfStart / dfEnd * payment.getPaymentYearFraction() / payment.getFixingYearFraction();
    //    s = new DoublesPair(tStart, -tStart * ratio);
    //    temp.add(s);
    //    s = new DoublesPair(tEnd, tEnd * ratio);
    //    temp.add(s);
    //
    //    result.put(liborCurveName, temp);
    //
    //    return result;
    return METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(payment, data).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_IBOR.presentValueCurveSensitivity(coupon, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIborGearing(final CouponIborGearing coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_IBOR_GEARING.presentValueCurveSensitivity(coupon, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIborCompounded(final CouponIborCompounded coupon, final YieldCurveBundle curves) {
    return METHOD_CPN_IBOR_COMP.presentValueCurveSensitivity(coupon, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponOIS(final CouponOIS payment, final YieldCurveBundle data) {
    return METHOD_CPN_OIS.presentValueCurveSensitivity(payment, data).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return METHOD_FRA.presentValueCurveSensitivity(fra, curves).getSensitivities();
  }

  /**
   * {@inheritDoc}
   * Future transaction pricing without convexity adjustment.
   */
  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    final InterestRateFutureDiscountingMethod method = InterestRateFutureDiscountingMethod.getInstance();
    return method.presentValueCurveSensitivity(future, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.presentValueCurveSensitivity(bond, curves).getSensitivities();
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
  public Map<String, List<DoublesPair>> visitBillSecurity(final BillSecurity bill, final YieldCurveBundle curves) {
    return METHOD_BILL_SECURITY.presentValueCurveSensitivity(bill, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitBillTransaction(final BillTransaction bill, final YieldCurveBundle curves) {
    return METHOD_BILL_TRANSACTION.presentValueCurveSensitivity(bill, curves).getSensitivities();
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
  public Map<String, List<DoublesPair>> visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle data) {
    final Map<String, List<DoublesPair>> map = new HashMap<String, List<DoublesPair>>();
    for (final Payment p : annuity.getPayments()) {
      final Map<String, List<DoublesPair>> tempMap = visit(p, data);
      for (final Map.Entry<String, List<DoublesPair>> entry : tempMap.entrySet()) {
        final String name = entry.getKey();
        if (!map.containsKey(name)) {
          map.put(name, entry.getValue());
        } else {
          final List<DoublesPair> tempList = map.get(name);
          tempList.addAll(entry.getValue());
          map.put(name, tempList);
        }
      }
    }
    return map;
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponCMS(final CouponCMS payment, final YieldCurveBundle data) {
    final CouponCMSDiscountingMethod method = CouponCMSDiscountingMethod.getInstance();
    return method.presentValueSensitivity(payment, data).getSensitivities();
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
