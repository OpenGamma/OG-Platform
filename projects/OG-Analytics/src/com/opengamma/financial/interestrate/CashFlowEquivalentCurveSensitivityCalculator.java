/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculator of the cash flow equivalent sensitivity to the curve. The result is a map of <Double, PresentValueSensitivity>. 
 * The cash flow equivalent sensitivity is represented by the double which is the time of the cash flow and the PresentValueSensitivity which is the sensitivity of the 
 * cash flow at that date.
 */
public class CashFlowEquivalentCurveSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<Double, InterestRateCurveSensitivity>> {

  /**
   * The unique instance of the calculator.
   */
  private static final CashFlowEquivalentCurveSensitivityCalculator s_instance = new CashFlowEquivalentCurveSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CashFlowEquivalentCurveSensitivityCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  CashFlowEquivalentCurveSensitivityCalculator() {
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    return new HashMap<Double, InterestRateCurveSensitivity>();
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitFixedCouponPayment(final CouponFixed coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    return new HashMap<Double, InterestRateCurveSensitivity>();
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitCouponIbor(final CouponIbor payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    double fixingStartTime = payment.getFixingPeriodStartTime();
    double fixingEndTime = payment.getFixingPeriodEndTime();
    double paymentTime = payment.getPaymentTime();
    final double beta = forwardCurve.getDiscountFactor(fixingStartTime) / forwardCurve.getDiscountFactor(fixingEndTime) * discountingCurve.getDiscountFactor(paymentTime)
        / discountingCurve.getDiscountFactor(fixingStartTime);
    double betaBar = payment.getNotional() * payment.getPaymentYearFraction() / payment.getFixingYearFraction();

    Map<Double, InterestRateCurveSensitivity> result = new HashMap<Double, InterestRateCurveSensitivity>();
    final Map<String, List<DoublesPair>> resultPVS = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    DoublesPair forwardStart = new DoublesPair(fixingStartTime, -fixingStartTime * beta * betaBar);
    listForward.add(forwardStart);
    DoublesPair forwardEnd = new DoublesPair(fixingEndTime, beta * fixingEndTime * betaBar);
    listForward.add(forwardEnd);
    resultPVS.put(payment.getForwardCurveName(), listForward);

    final List<DoublesPair> listDisc = new ArrayList<DoublesPair>();
    DoublesPair discStart = new DoublesPair(fixingStartTime, beta * fixingStartTime * betaBar);
    listDisc.add(discStart);
    DoublesPair discPay = new DoublesPair(paymentTime, -paymentTime * beta * betaBar);
    listDisc.add(discPay);
    resultPVS.put(payment.getFundingCurveName(), listDisc);

    result.put(fixingStartTime, new InterestRateCurveSensitivity(resultPVS));
    return result;
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    Map<Double, InterestRateCurveSensitivity> result = new HashMap<Double, InterestRateCurveSensitivity>();
    for (final Payment p : annuity.getPayments()) {
      Map<Double, InterestRateCurveSensitivity> paymentSensi = visit(p, curves);
      result.putAll(paymentSensi);
      // It is suppose that no two coupons have the same cfe sensitivity date.
    }
    return result;
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitForwardLiborAnnuity(final AnnuityCouponIbor annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    Map<Double, InterestRateCurveSensitivity> result = new HashMap<Double, InterestRateCurveSensitivity>();
    Map<Double, InterestRateCurveSensitivity> legSensi1 = visit(swap.getFirstLeg(), curves);
    result.putAll(legSensi1);
    Map<Double, InterestRateCurveSensitivity> legSensi2 = visit(swap.getSecondLeg(), curves);
    result.putAll(legSensi2);
    // It is suppose that the two legs have different cfe sensitivity date.
    return result;
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

}
