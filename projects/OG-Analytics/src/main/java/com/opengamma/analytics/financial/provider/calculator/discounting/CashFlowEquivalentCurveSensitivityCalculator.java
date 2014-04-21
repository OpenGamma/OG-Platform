/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculator of the cash flow equivalent sensitivity to the curve. The result is a map of <Double, PresentValueSensitivity>.
 * The cash flow equivalent sensitivity is represented by the double which is the time of the cash flow and the PresentValueSensitivity which is the sensitivity of the
 * cash flow at that date.
 */
public class CashFlowEquivalentCurveSensitivityCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, Map<Double, MulticurveSensitivity>> {

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
  public Map<Double, MulticurveSensitivity> visitFixedPayment(final PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    return new HashMap<>();
  }

  @Override
  public Map<Double, MulticurveSensitivity> visitCouponFixed(final CouponFixed coupon, final MulticurveProviderInterface multicurves) {
    return new HashMap<>();
  }

  @Override
  public Map<Double, MulticurveSensitivity> visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(payment, "Payment");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Currency ccy = payment.getCurrency();
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double dfRatio = multicurves.getDiscountFactor(ccy, paymentTime) / multicurves.getDiscountFactor(ccy, fixingStartTime);
    final double af = payment.getFixingAccrualFactor();
    final double beta = (1.0 + af * multicurves.getSimplyCompoundForwardRate(payment.getIndex(), fixingStartTime, fixingEndTime, payment.getFixingAccrualFactor())) * dfRatio;
    final double betaBar = payment.getNotional() * payment.getPaymentYearFraction() / af;
    final double forwardBar = af * dfRatio * betaBar;

    final Map<Double, MulticurveSensitivity> result = new HashMap<>();
    final Map<String, List<ForwardSensitivity>> resultFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(fixingStartTime, fixingEndTime, af, forwardBar));
    resultFwd.put(multicurves.getName(payment.getIndex()), listForward);

    final Map<String, List<DoublesPair>> resultDsc = new HashMap<>();
    final List<DoublesPair> listDisc = new ArrayList<>();
    final DoublesPair discStart = DoublesPair.of(fixingStartTime, beta * fixingStartTime * betaBar);
    listDisc.add(discStart);
    final DoublesPair discPay = DoublesPair.of(paymentTime, -paymentTime * beta * betaBar);
    listDisc.add(discPay);
    resultDsc.put(multicurves.getName(ccy), listDisc);

    result.put(fixingStartTime, MulticurveSensitivity.of(resultDsc, resultFwd));
    return result;
  }

  @Override
  public Map<Double, MulticurveSensitivity> visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(payment, "Payment");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Currency ccy = payment.getCurrency();
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double dfRatio = multicurves.getDiscountFactor(ccy, paymentTime) / multicurves.getDiscountFactor(ccy, fixingStartTime);
    final double af = payment.getFixingAccrualFactor();
    final double beta = (1.0 + af * multicurves.getSimplyCompoundForwardRate(payment.getIndex(), fixingStartTime, fixingEndTime, payment.getFixingAccrualFactor())) * dfRatio;
    final double betaBar = payment.getNotional() * payment.getPaymentYearFraction() / af;
    final double forwardBar = af * dfRatio * betaBar;

    final Map<Double, MulticurveSensitivity> result = new HashMap<>();
    final Map<String, List<ForwardSensitivity>> resultFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(fixingStartTime, fixingEndTime, af, forwardBar));
    resultFwd.put(multicurves.getName(payment.getIndex()), listForward);

    final Map<String, List<DoublesPair>> resultDsc = new HashMap<>();
    final List<DoublesPair> listDisc = new ArrayList<>();
    final DoublesPair discStart = DoublesPair.of(fixingStartTime, beta * fixingStartTime * betaBar);
    listDisc.add(discStart);
    final DoublesPair discPay = DoublesPair.of(paymentTime, -paymentTime * beta * betaBar);
    listDisc.add(discPay);
    resultDsc.put(multicurves.getName(ccy), listDisc);

    result.put(fixingStartTime, MulticurveSensitivity.of(resultDsc, resultFwd));
    return result;
  }

  @Override
  public Map<Double, MulticurveSensitivity> visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Map<Double, MulticurveSensitivity> result = new HashMap<>();
    for (final Payment p : annuity.getPayments()) {
      final Map<Double, MulticurveSensitivity> paymentSensi = p.accept(this, multicurves);
      result.putAll(paymentSensi);
      // It is suppose that no two coupons have the same cfe sensitivity date.
    }
    return result;
  }

  @Override
  public Map<Double, MulticurveSensitivity> visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface multicurves) {
    return visitGenericAnnuity(annuity, multicurves);
  }

  @Override
  public Map<Double, MulticurveSensitivity> visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(swap, "Swap");
    ArgumentChecker.notNull(multicurves, "Multicurves provider");
    final Map<Double, MulticurveSensitivity> result = new HashMap<>();
    final Map<Double, MulticurveSensitivity> legSensi1 = swap.getFirstLeg().accept(this, multicurves);
    result.putAll(legSensi1);
    final Map<Double, MulticurveSensitivity> legSensi2 = swap.getSecondLeg().accept(this, multicurves);
    result.putAll(legSensi2);
    // It is suppose that the two legs have different cfe sensitivity date.
    return result;
  }

  @Override
  public Map<Double, MulticurveSensitivity> visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurves) {
    return visitSwap(swap, multicurves);
  }

}
