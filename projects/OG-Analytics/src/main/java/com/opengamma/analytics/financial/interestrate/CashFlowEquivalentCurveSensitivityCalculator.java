/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculator of the cash flow equivalent sensitivity to the curve. The result is a map of <Double, PresentValueSensitivity>.
 * The cash flow equivalent sensitivity is represented by the double which is the time of the cash flow and the PresentValueSensitivity which is the sensitivity of the
 * cash flow at that date.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class CashFlowEquivalentCurveSensitivityCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Map<Double, InterestRateCurveSensitivity>> {

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
  public Map<Double, InterestRateCurveSensitivity> visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    return new HashMap<>();
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    return new HashMap<>();
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitCouponIbor(final CouponIbor payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double beta = forwardCurve.getDiscountFactor(fixingStartTime) / forwardCurve.getDiscountFactor(fixingEndTime) * discountingCurve.getDiscountFactor(paymentTime)
        / discountingCurve.getDiscountFactor(fixingStartTime);
    final double betaBar = payment.getNotional() * payment.getPaymentYearFraction() / payment.getFixingAccrualFactor();

    final Map<Double, InterestRateCurveSensitivity> result = new HashMap<>();
    final Map<String, List<DoublesPair>> resultPVS = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    final DoublesPair forwardStart = DoublesPair.of(fixingStartTime, -fixingStartTime * beta * betaBar);
    listForward.add(forwardStart);
    final DoublesPair forwardEnd = DoublesPair.of(fixingEndTime, beta * fixingEndTime * betaBar);
    listForward.add(forwardEnd);
    resultPVS.put(payment.getForwardCurveName(), listForward);

    final List<DoublesPair> listDisc = new ArrayList<>();
    final DoublesPair discStart = DoublesPair.of(fixingStartTime, beta * fixingStartTime * betaBar);
    listDisc.add(discStart);
    final DoublesPair discPay = DoublesPair.of(paymentTime, -paymentTime * beta * betaBar);
    listDisc.add(discPay);
    resultPVS.put(payment.getFundingCurveName(), listDisc);

    result.put(fixingStartTime, new InterestRateCurveSensitivity(resultPVS));
    return result;
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitCouponIborSpread(final CouponIborSpread payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double beta = forwardCurve.getDiscountFactor(fixingStartTime) / forwardCurve.getDiscountFactor(fixingEndTime) * discountingCurve.getDiscountFactor(paymentTime)
        / discountingCurve.getDiscountFactor(fixingStartTime);
    final double betaBar = payment.getNotional() * payment.getPaymentYearFraction() / payment.getFixingAccrualFactor();
    final Map<Double, InterestRateCurveSensitivity> result = new HashMap<>();
    final Map<String, List<DoublesPair>> resultPVS = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    final DoublesPair forwardStart = DoublesPair.of(fixingStartTime, -fixingStartTime * beta * betaBar);
    listForward.add(forwardStart);
    final DoublesPair forwardEnd = DoublesPair.of(fixingEndTime, beta * fixingEndTime * betaBar);
    listForward.add(forwardEnd);
    resultPVS.put(payment.getForwardCurveName(), listForward);

    final List<DoublesPair> listDisc = new ArrayList<>();
    final DoublesPair discStart = DoublesPair.of(fixingStartTime, beta * fixingStartTime * betaBar);
    listDisc.add(discStart);
    final DoublesPair discPay = DoublesPair.of(paymentTime, -paymentTime * beta * betaBar);
    listDisc.add(discPay);
    resultPVS.put(payment.getFundingCurveName(), listDisc);

    result.put(fixingStartTime, new InterestRateCurveSensitivity(resultPVS));
    return result;
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    final Map<Double, InterestRateCurveSensitivity> result = new HashMap<>();
    for (final Payment p : annuity.getPayments()) {
      final Map<Double, InterestRateCurveSensitivity> paymentSensi = p.accept(this, curves);
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
  public Map<Double, InterestRateCurveSensitivity> visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    final Map<Double, InterestRateCurveSensitivity> result = new HashMap<>();
    final Map<Double, InterestRateCurveSensitivity> legSensi1 = swap.getFirstLeg().accept(this, curves);
    result.putAll(legSensi1);
    final Map<Double, InterestRateCurveSensitivity> legSensi2 = swap.getSecondLeg().accept(this, curves);
    result.putAll(legSensi2);
    // It is suppose that the two legs have different cfe sensitivity date.
    return result;
  }

  @Override
  public Map<Double, InterestRateCurveSensitivity> visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

}
