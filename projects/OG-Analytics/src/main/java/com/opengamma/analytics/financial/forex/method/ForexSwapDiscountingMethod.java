/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex swap transactions by discounting each payment.
 * @deprecated Use {@link com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingMethod}
 */
@Deprecated
public final class ForexSwapDiscountingMethod implements ForexPricingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexSwapDiscountingMethod INSTANCE = new ForexSwapDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexSwapDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexSwapDiscountingMethod() {
  }

  /**
   * Forex method by discounting.
   */
  private static final ForexDiscountingMethod METHOD_FX = ForexDiscountingMethod.getInstance();

  /**
   * Compute the present value by discounting the payments in their own currency.
   * @param fx The Forex swap.
   * @param curves The curve bundle containing the discounting curves.
   * @return The multi-currency present value.
   */
  public MultipleCurrencyAmount presentValue(final ForexSwap fx, final YieldCurveBundle curves) {
    final MultipleCurrencyAmount pv = METHOD_FX.presentValue(fx.getNearLeg(), curves);
    return pv.plus(METHOD_FX.presentValue(fx.getFarLeg(), curves));
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexSwap, "Instrument should be ForexSwap");
    return presentValue((ForexSwap) instrument, curves);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return presentValue(instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a forex swap transaction.
   * @param fx The forex swap transaction.
   * @param curves The curves.
   * @return The sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexSwap fx, final YieldCurveBundle curves) {
    MultipleCurrencyInterestRateCurveSensitivity result = METHOD_FX.presentValueCurveSensitivity(fx.getNearLeg(), curves);
    result = result.plus(METHOD_FX.presentValueCurveSensitivity(fx.getFarLeg(), curves));
    return result;
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexSwap, "Forex swap");
    return presentValueCurveSensitivity((ForexSwap) instrument, curves);
  }

  /**
   * The par spread is the spread that should be added to the forex forward points to have a zero value.
   * @param fx The forex swap.
   * @param curves The yield curve bundle with the relevant exchange rates.
   * @return The spread.
   */
  public double parSpread(final ForexSwap fx, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(fx, "Forex swap");
    ArgumentChecker.notNull(curves, "Curve bundle");
    final double pv2 = curves.getFxRates().convert(presentValue(fx, curves), fx.getNearLeg().getCurrency2()).getAmount();
    final double dfEnd = curves.getCurve(fx.getFarLeg().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(fx.getFarLeg().getPaymentTime());
    final double notional1 = fx.getNearLeg().getPaymentCurrency1().getAmount();
    return -pv2 / (notional1 * dfEnd);
  }

  /**
   * Computes the par spread curve sensitivity.
   * @param fx The forex swap.
   * @param curves The yield curve bundle with the relevant exchange rates.
   * @return The par spread sensitivity.
   */
  public InterestRateCurveSensitivity parSpreadCurveSensitivity(final ForexSwap fx, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(fx, "Forex swap");
    ArgumentChecker.notNull(curves, "Curve bundle");
    final Currency ccy2 = fx.getNearLeg().getCurrency2();
    final String name2 = fx.getFarLeg().getPaymentCurrency2().getFundingCurveName();
    final double payTime = fx.getFarLeg().getPaymentTime();
    final double pv2 = curves.getFxRates().convert(presentValue(fx, curves), ccy2).getAmount();
    final double dfEnd = curves.getCurve(name2).getDiscountFactor(payTime);
    final double notional1 = fx.getNearLeg().getPaymentCurrency1().getAmount();
    //    double spread = -pv2 / (notional1 * dfEnd);
    // Backward sweep
    final double spreadBar = 1.0;
    final double dfEndBar = pv2 / (notional1 * dfEnd * dfEnd) * spreadBar;
    final double pv2Bar = -spreadBar / (notional1 * dfEnd);
    final MultipleCurrencyInterestRateCurveSensitivity pv2DrMC = presentValueCurveSensitivity(fx, curves);
    final InterestRateCurveSensitivity pv2Dr = pv2DrMC.converted(ccy2, curves.getFxRates()).getSensitivity(ccy2);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(payTime, -payTime * dfEnd * dfEndBar));
    final InterestRateCurveSensitivity dfEndDr = InterestRateCurveSensitivity.of(name2, list);
    return pv2Dr.multipliedBy(pv2Bar).plus(dfEndDr);
  }

}
