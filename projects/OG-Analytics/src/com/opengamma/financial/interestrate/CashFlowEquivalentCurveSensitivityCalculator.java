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
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class CashFlowEquivalentCurveSensitivityCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Map<Double, PresentValueSensitivity>> {

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
  public Map<Double, PresentValueSensitivity> visit(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Map<Double, PresentValueSensitivity> visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    return new HashMap<Double, PresentValueSensitivity>();
  }

  @Override
  public Map<Double, PresentValueSensitivity> visitFixedCouponPayment(final CouponFixed payment, final YieldCurveBundle curves) {
    return visitFixedPayment(payment, curves);
  }

  @Override
  public Map<Double, PresentValueSensitivity> visitCouponIbor(final CouponIbor payment, final YieldCurveBundle curves) {
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

    Map<Double, PresentValueSensitivity> result = new HashMap<Double, PresentValueSensitivity>();
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

    result.put(fixingStartTime, new PresentValueSensitivity(resultPVS));
    return result;
  }

  @Override
  public Map<Double, PresentValueSensitivity> visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    Map<Double, PresentValueSensitivity> result = new HashMap<Double, PresentValueSensitivity>();
    for (final Payment p : annuity.getPayments()) {
      Map<Double, PresentValueSensitivity> paymentSensi = visit(p, curves);
      result.putAll(paymentSensi);
      // It is suppose that no two coupon have the same cfe sensitivity date.
    }
    return result;
  }

  @Override
  public Map<Double, PresentValueSensitivity> visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public Map<Double, PresentValueSensitivity> visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    Map<Double, PresentValueSensitivity> result = new HashMap<Double, PresentValueSensitivity>();
    Map<Double, PresentValueSensitivity> legSensi1 = visit(swap.getFirstLeg(), curves);
    result.putAll(legSensi1);
    Map<Double, PresentValueSensitivity> legSensi2 = visit(swap.getSecondLeg(), curves);
    result.putAll(legSensi2);
    // It is suppose that the two legs have the different cfe sensitivity date.
    return result;
  }

  @Override
  public Map<Double, PresentValueSensitivity> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

}
