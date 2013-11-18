/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveForwardPointsProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Pricing method for Forex transactions (spot or forward) by forward points.
 * <p>Documentation: Forex Swaps and Cross-currency Swaps. OpenGamma Documentation n. 21
 */
public final class ForexForwardPointsMethod {

  /**
   * The method unique instance.
   */
  private static final ForexForwardPointsMethod INSTANCE = new ForexForwardPointsMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexForwardPointsMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexForwardPointsMethod() {
  }

  /**
   * Compute the present value by estimating the forward points. The present value is computed in the second currency in the ccyPair.
   * No coherence check is done between the interest curves and the forward points curves.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curves provider.
   * @param forwardRates The curve with the forward points.
   * @param ccyPair The ordered currency pair for which the forwardRates are valid.
   * @return The multi-currency present value (in currency 2).
   */
  public MultipleCurrencyAmount presentValue(final Forex fx, final MulticurveProviderInterface multicurves, final DoublesCurve forwardRates, final Pair<Currency, Currency> ccyPair) {
    final double payTime = fx.getPaymentTime();
    final double fwdRate = forwardRates.getYValue(payTime);
    final Currency ccy2;
    final double amount1;
    final double amount2;
    // Implementation note: The "if" is used to check in which order the fx is given with respect to the order of the forward rates.
    if (checkCurrency(fx, ccyPair)) { // Currency1 of fx is first currency in ccyPair
      ccy2 = fx.getCurrency2();
      amount1 = fx.getPaymentCurrency1().getAmount();
      amount2 = fx.getPaymentCurrency2().getAmount();
    } else {
      ccy2 = fx.getCurrency1();
      amount1 = fx.getPaymentCurrency2().getAmount();
      amount2 = fx.getPaymentCurrency1().getAmount();
    }
    final double df2 = multicurves.getDiscountFactor(ccy2, payTime);
    final double pv = df2 * (amount2 + amount1 * fwdRate);
    return MultipleCurrencyAmount.of(ccy2, pv);
  }

  /**
   * Returns the present value for data stored in a unique Provider.
   * @param fx The Forex derivative.
   * @param multicurvesForward The multi-curve provider with forward rates for a currency pair.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final Forex fx, final MulticurveForwardPointsProviderInterface multicurvesForward) {
    ArgumentChecker.notNull(multicurvesForward, "multi-curve provider");
    return presentValue(fx, multicurvesForward.getMulticurveProvider(), multicurvesForward.getForwardPointsCurve(), multicurvesForward.getCurrencyPair());
  }

  /**
   * Compute the currency exposure by estimating the forward points.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curves provider.
   * @param forwardRates The curve with the forward points. The order of the currencies is the order of ccy0 and ccy1 in the FXMatrix of the multicurves provider.
   * @param ccyPair The ordered currency pair for which the forwardRates are valid.
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final Forex fx, final MulticurveProviderInterface multicurves, final DoublesCurve forwardRates, final Pair<Currency, Currency> ccyPair) {
    final double payTime = fx.getPaymentTime();
    final double fwdRate = forwardRates.getYValue(payTime);
    final Currency ccy1;
    final Currency ccy2;
    final double amount1;
    final double amount2;
    // Implementation note: The "if" is used to check in which order the fx is given with respect to the order of the forward rates.
    if (checkCurrency(fx, ccyPair)) { // Currency1 of fx is first currency in ccyPair
      ccy1 = fx.getCurrency1();
      ccy2 = fx.getCurrency2();
      amount1 = fx.getPaymentCurrency1().getAmount();
      amount2 = fx.getPaymentCurrency2().getAmount();
    } else {
      ccy1 = fx.getCurrency2();
      ccy2 = fx.getCurrency1();
      amount1 = fx.getPaymentCurrency2().getAmount();
      amount2 = fx.getPaymentCurrency1().getAmount();
    }
    final double df2 = multicurves.getDiscountFactor(ccy2, payTime);
    final double ce2 = amount2 * df2;
    final double todayRate = multicurves.getFxRate(ccy1, ccy2);
    final double ce1 = amount1 * df2 * (fwdRate / todayRate);
    MultipleCurrencyAmount ce = MultipleCurrencyAmount.of(ccy1, ce1);
    ce = ce.plus(ccy2, ce2);
    return ce;
  }

  /**
   * Computes the present value curve sensitivity for forex by forward point method.
   * The sensitivity is only to the final discounting, not to the forward points.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curve provider.
   * @param forwardRates The curve with the forward points. The order of the currencies is the order of ccy0 and ccy1 in the FXMatrix of the multicurves provider.
   * @param ccyPair The ordered currency pair for which the forwardRates are valid.
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final Forex fx, final MulticurveProviderInterface multicurves, final DoublesCurve forwardRates,
      final Pair<Currency, Currency> ccyPair) {
    final double payTime = fx.getPaymentTime();
    final double fwdRate = forwardRates.getYValue(payTime);
    final Currency ccy2;
    final double amount1;
    final double amount2;
    // Implementation note: The "if" is used to check in which order the fx is given with respect to the order of the forward rates.
    if (checkCurrency(fx, ccyPair)) { // Currency1 of fx is first currency in ccyPair
      ccy2 = fx.getCurrency2();
      amount1 = fx.getPaymentCurrency1().getAmount();
      amount2 = fx.getPaymentCurrency2().getAmount();
    } else {
      ccy2 = fx.getCurrency1();
      amount1 = fx.getPaymentCurrency2().getAmount();
      amount2 = fx.getPaymentCurrency1().getAmount();
    }
    final double df2 = multicurves.getDiscountFactor(ccy2, payTime);
    //    final double pv = df2 * (amount2 + amount1 * fwdRate);
    // Backward sweep
    final double pvBar = 1.0;
    final double df2Bar = (amount2 + amount1 * fwdRate) * pvBar;
    final DoublesPair s = DoublesPair.of(payTime, -payTime * df2 * df2Bar);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    result.put(multicurves.getName(ccy2), list);
    return MultipleCurrencyMulticurveSensitivity.of(ccy2, MulticurveSensitivity.ofYieldDiscounting(result));
  }

  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final Forex fx, final MulticurveForwardPointsProviderInterface multicurvesForward) {
    ArgumentChecker.notNull(multicurvesForward, "multi-curve provider");
    return presentValueCurveSensitivity(fx, multicurvesForward.getMulticurveProvider(), multicurvesForward.getForwardPointsCurve(), multicurvesForward.getCurrencyPair());
  }

  /**
   * Computes the sensitivity of the present value to the figures in the forward points curves.
   * @param fx The Forex derivative.
   * @param multicurves The multi-curve provider.
   * @param forwardRates The curve with the forward points. The order of the currencies is the order of ccy0 and ccy1 in the FXMatrix of the multicurves provider.
   * @param ccyPair The ordered currency pair for which the forwardRates are valid.
   * @return The sensitivity.
   */
  public double[] presentValueForwardPointsSensitivity(final Forex fx, final MulticurveProviderInterface multicurves, final DoublesCurve forwardRates, final Pair<Currency, Currency> ccyPair) {
    final double payTime = fx.getPaymentTime();
    final Currency ccy2;
    final double amount1;
    // Implementation note: The "if" is used to check in which order the fx is given with respect to the order of the forward rates.
    if (checkCurrency(fx, ccyPair)) { // Currency1 of fx is first currency in ccyPair
      ccy2 = fx.getCurrency2();
      amount1 = fx.getPaymentCurrency1().getAmount();
    } else {
      ccy2 = fx.getCurrency1();
      amount1 = fx.getPaymentCurrency2().getAmount();
    }
    final double df2 = multicurves.getDiscountFactor(ccy2, payTime);
    // Backward sweep
    final double pvBar = 1.0;
    final double fwdRateBar = df2 * amount1 * pvBar;
    final Double[] fwdRateDp = forwardRates.getYValueParameterSensitivity(payTime);
    final double[] sensitivity = new double[fwdRateDp.length];
    for (int loops = 0; loops < fwdRateDp.length; loops++) {
      sensitivity[loops] = fwdRateDp[loops] * fwdRateBar;
    }
    return sensitivity;
  }

  /**
   * Check that the Forex currencies are compatible with the currency pair. Returns a flag indicating if the first currency in the Forex is the first currency in the pair.
   * @param fx The Forex derivative.
   * @param ccyPair The ordered currency pair.
   * @return The first currency flag.
   */
  private boolean checkCurrency(final Forex fx, final Pair<Currency, Currency> ccyPair) {
    ArgumentChecker.isTrue(fx.getCurrency1().equals(ccyPair.getFirst()) || fx.getCurrency1().equals(ccyPair.getSecond()), fx.getCurrency1() + "not in currency pair for forward point method");
    ArgumentChecker.isTrue(fx.getCurrency2().equals(ccyPair.getFirst()) || fx.getCurrency2().equals(ccyPair.getSecond()), fx.getCurrency2() + "not in currency pair for forward point method");
    return fx.getCurrency1().equals(ccyPair.getFirst());
  }

}
