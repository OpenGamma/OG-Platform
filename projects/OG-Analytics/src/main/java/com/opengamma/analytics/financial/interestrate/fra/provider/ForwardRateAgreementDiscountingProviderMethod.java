/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.fra.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
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
 */
public final class ForwardRateAgreementDiscountingProviderMethod {

  /**
   * The method unique instance.
   */
  private static final ForwardRateAgreementDiscountingProviderMethod INSTANCE = new ForwardRateAgreementDiscountingProviderMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForwardRateAgreementDiscountingProviderMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForwardRateAgreementDiscountingProviderMethod() {
  }

  /**
   * Compute the present value of a FRA by discounting.
   * @param fra The FRA.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(fra, "FRA");
    ArgumentChecker.notNull(multicurve, "Multiurves");
    final double discountFactorSettlement = multicurve.getDiscountFactor(fra.getCurrency(), fra.getPaymentTime());
    final double forward = multicurve.getSimplyCompoundForwardRate(fra.getIndex(), fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction());
    final double presentValue = discountFactorSettlement * fra.getPaymentYearFraction() * fra.getNotional() * (forward - fra.getRate()) / (1 + fra.getPaymentYearFraction() * forward);
    return MultipleCurrencyAmount.of(fra.getCurrency(), presentValue);
  }

  /**
   * Compute the present value sensitivity to rates of a FRA by discounting.
   * @param fra The FRA.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(fra, "FRA");
    ArgumentChecker.notNull(multicurve, "Multiurves");
    final double df = multicurve.getDiscountFactor(fra.getCurrency(), fra.getPaymentTime());
    final double forward = multicurve.getSimplyCompoundForwardRate(fra.getIndex(), fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction());
    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = df * fra.getPaymentYearFraction() * fra.getNotional() * (1 - (forward - fra.getRate()) / (1 + fra.getPaymentYearFraction() * forward) * fra.getPaymentYearFraction())
        / (1 + fra.getPaymentYearFraction() * forward);
    final double dfBar = fra.getPaymentYearFraction() * fra.getNotional() * (forward - fra.getRate()) / (1 + fra.getFixingYearFraction() * forward) * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(fra.getPaymentTime(), -fra.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(fra.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction(), forwardBar));
    mapFwd.put(multicurve.getName(fra.getIndex()), listForward);
    final MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(fra.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
    return result;
  }

  /**
   * Compute the par rate or forward rate of the FRA.
   * @param fra The FRA.
   * @param multicurve The multi-curve provider.
   * @return The par rate.
   */
  public double parRate(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(fra, "FRA");
    ArgumentChecker.notNull(multicurve, "Multiurves");
    return multicurve.getSimplyCompoundForwardRate(fra.getIndex(), fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction());
  }

  /**
   * Computes the par spread (spread to be added to the fixed rate to have a present value of 0).
   * @param fra The FRA.
   * @param multicurve The multi-curve provider.
   * @return The par spread.
   */
  public double parSpread(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(fra, "FRA");
    ArgumentChecker.notNull(multicurve, "Multiurves");
    final double forward = multicurve.getSimplyCompoundForwardRate(fra.getIndex(), fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction());
    return forward - fra.getRate();
  }

  /**
   * Computes the par spread curve sensitivity.
   * @param fra The FRA.
   * @param multicurve The multi-curve provider.
   * @return The par spread sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(fra, "FRA");
    ArgumentChecker.notNull(multicurve, "Multiurves");
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(), fra.getFixingYearFraction(), 1.0));
    mapFwd.put(multicurve.getName(fra.getIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

}
