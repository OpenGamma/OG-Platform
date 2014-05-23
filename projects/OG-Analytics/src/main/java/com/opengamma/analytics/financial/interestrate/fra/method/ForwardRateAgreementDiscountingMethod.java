/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.fra.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the present value and its sensitivities for a FRA with
 * discounting. The present value is computed as the (forward rate - FRA rate)
 * multiplied by the notional and the payment accrual factor and discounted to
 * settlement. The discounting to settlement is done using the forward rate
 * over the fixing period. The value is further discounted from settlement to
 * today using the discounting curve.
 * $$
 * \begin{equation*}
 * P^D(0,t_1)\frac{\delta_P(F-K)}{1+\delta_P F} \quad \mbox{and}\quad F = \frac{1}{\delta_F}\left( \frac{P^j(0,t_1)}{P^j(0,t_2)}-1\right)
 * \end{equation*}
 * $$
 * This approach is valid subject to a independence hypothesis between the
 * discounting curve and some spread.
 * <p>
 * Reference: Henrard, M. (2010). The irony in the derivatives discounting part
 * II: the crisis. Wilmott Journal, 2(6):301-316.
 * @deprecated Use {@link ForwardRateAgreementDiscountingProviderMethod}
 */
@Deprecated
public final class ForwardRateAgreementDiscountingMethod implements PricingMethod {
  private static final ForwardRateAgreementDiscountingMethod INSTANCE = new ForwardRateAgreementDiscountingMethod();

  public static ForwardRateAgreementDiscountingMethod getInstance() {
    return INSTANCE;
  }

  private ForwardRateAgreementDiscountingMethod() {
  }

  /**
   * Compute the present value of a FRA by discounting.
   * @param fra The FRA.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    final double discountFactorSettlement = discountingCurve.getDiscountFactor(fra.getPaymentTime());
    final double forward = (forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime()) - 1) / fra.getFixingYearFraction();
    final double presentValue = discountFactorSettlement * fra.getPaymentYearFraction() * fra.getNotional() * (forward - fra.getRate()) / (1 + fra.getPaymentYearFraction() * forward);
    return CurrencyAmount.of(fra.getCurrency(), presentValue);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForwardRateAgreement, "Forward rate agreement");
    return presentValue((ForwardRateAgreement) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a FRA by discounting.
   * @param fra The FRA.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The present value sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    final double df = discountingCurve.getDiscountFactor(fra.getPaymentTime());
    final double dfForwardStart = forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime());
    final double forward = (dfForwardStart / dfForwardEnd - 1.0) / fra.getFixingYearFraction();
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = df * fra.getPaymentYearFraction() * fra.getNotional() * (1 - (forward - fra.getRate()) / (1 + fra.getPaymentYearFraction() * forward) * fra.getPaymentYearFraction())
        / (1 + fra.getPaymentYearFraction() * forward);
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / fra.getFixingYearFraction() * forwardBar;
    final double dfForwardStartBar = 1.0 / (fra.getFixingYearFraction() * dfForwardEnd) * forwardBar;
    final double dfBar = fra.getPaymentYearFraction() * fra.getNotional() * (forward - fra.getRate()) / (1 + fra.getFixingYearFraction() * forward) * pvBar;
    final Map<String, List<DoublesPair>> resultMapDiscouting = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(fra.getPaymentTime(), -fra.getPaymentTime() * df * dfBar));
    resultMapDiscouting.put(fra.getFundingCurveName(), listDiscounting);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMapDiscouting);
    final Map<String, List<DoublesPair>> resultMapForward = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(fra.getFixingPeriodStartTime(), -fra.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(DoublesPair.of(fra.getFixingPeriodEndTime(), -fra.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMapForward.put(fra.getForwardCurveName(), listForward);
    return result.plus(new InterestRateCurveSensitivity(resultMapForward));
  }

  /**
   * Compute the par rate or forward rate of the FRA.
   * @param fra The FRA.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The par rate.
   */
  public double parRate(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime()) - 1) / fra.getFixingYearFraction();
    return forward;
  }

  /**
   * Compute the par rate sensitivity to the rates for a FRA.
   * @param fra The FRA.
   * @param curves The yield curves. Should contain the discounting and forward curves associated.
   * @return The par rate sensitivity.
   */
  public InterestRateCurveSensitivity parRateCurveSensitivity(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    final double dfForwardStart = forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime());
    final double dfForwardEnd = forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime());
    // Backward sweep
    final double forwardBar = 1.0;
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / fra.getFixingYearFraction() * forwardBar;
    final double dfForwardStartBar = 1.0 / (fra.getFixingYearFraction() * dfForwardEnd) * forwardBar;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForward = new ArrayList<>();
    listForward.add(DoublesPair.of(fra.getFixingPeriodStartTime(), -fra.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar));
    listForward.add(DoublesPair.of(fra.getFixingPeriodEndTime(), -fra.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar));
    resultMap.put(fra.getForwardCurveName(), listForward);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    return result;
  }

  /**
   * Computes the sensitivity of the present value of a FRA with notional 1 to the change of fixed rate.
   * @param fra The FRA.
   * @param curves The curve bundle.
   * @return The sensitivity.
   */
  public double presentValueCouponSensitivity(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(fra.getFundingCurveName());
    final YieldAndDiscountCurve liborCurve = curves.getCurve(fra.getForwardCurveName());
    final double fixingAF = fra.getFixingYearFraction();
    final double paymentAF = fra.getPaymentYearFraction();
    final double forward = (liborCurve.getDiscountFactor(fra.getFixingPeriodStartTime()) / liborCurve.getDiscountFactor(fra.getFixingPeriodEndTime()) - 1.0) / fixingAF;
    final double res = -fundingCurve.getDiscountFactor(fra.getPaymentTime()) * paymentAF / (1 + forward * paymentAF);
    return res;
  }

  /**
   * Computes the par spread (spread to be added to the fixed rate to have a present value of 0).
   * @param fra The FRA.
   * @param curves The yield curve bundle.
   * @return The par spread.
   */
  public double parSpread(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    final double dfStart = forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime());
    final double dfEnd = forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime());
    final double forward = (dfStart / dfEnd - 1) / fra.getFixingYearFraction();
    return forward - fra.getRate();
  }

  /**
   * Computes the par spread curve sensitivity.
   * @param fra The FRA.
   * @param curves The yield curve bundle.
   * @return The par spread sensitivity.
   */
  public InterestRateCurveSensitivity parSpreadCurveSensitivity(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(fra, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(fra.getForwardCurveName());
    final double dfStart = forwardCurve.getDiscountFactor(fra.getFixingPeriodStartTime());
    final double dfEnd = forwardCurve.getDiscountFactor(fra.getFixingPeriodEndTime());
    // Backward sweep
    final double parSpreadBar = 1.0;
    final double dfEndBar = -dfStart / (dfEnd * dfEnd * fra.getFixingYearFraction()) * parSpreadBar;
    final double dfStartBar = 1 / (dfEnd * fra.getFixingYearFraction()) * parSpreadBar;
    final Map<String, List<DoublesPair>> resultMapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(fra.getFixingPeriodStartTime(), -fra.getFixingPeriodStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(fra.getFixingPeriodEndTime(), -fra.getFixingPeriodEndTime() * dfEnd * dfEndBar));
    resultMapDsc.put(fra.getForwardCurveName(), listDiscounting);
    return new InterestRateCurveSensitivity(resultMapDsc);
  }

}
