/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackFlatMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackForexTermStructureBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for vanilla Forex option transactions with Black function and a volatility provider.
 * OG-Implementation: Vanilla Forex options: Garman-Kohlhagen and risk reversal/strangle, version 1.5, May 2012.
 * @deprecated Use {@link ForexOptionVanillaBlackFlatMethod}
 */
@Deprecated
public final class ForexOptionVanillaBlackTermStructureMethod implements ForexPricingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexOptionVanillaBlackTermStructureMethod INSTANCE = new ForexOptionVanillaBlackTermStructureMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexOptionVanillaBlackTermStructureMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexOptionVanillaBlackTermStructureMethod() {
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Computes the present value of the vanilla option with the Black function and a volatility from a volatility surface.
   * @param optionForex The Forex option.
   * @param black The curve and smile data.
   * @return The present value. The value is in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(optionForex);
    final double price = func.evaluate(dataBlack) * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackForexTermStructureBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionVanilla) instrument, (YieldCurveWithBlackForexTermStructureBundle) curves);
  }

  /**
   * Computes the implied Black volatility of the vanilla option.
   * @param optionForex The Forex option.
   * @param curves The curve and Black data.
   * @return The implied volatility.
   */
  public double impliedVolatility(final ForexOptionVanilla optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackForexTermStructureBundle, "Yield curve bundle should contain Black volatility data");
    final YieldCurveWithBlackForexTermStructureBundle black = (YieldCurveWithBlackForexTermStructureBundle) curves;
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with data");
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    return volatility;
  }

  /**
   * Computes the currency exposure of the vanilla option with the Black function and a volatility from a volatility surface. The exposure is computed in both option currencies.
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @return The currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(optionForex, dataBlack);
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    final double price = priceAdjoint[0] * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * sign;
    final double deltaSpot = priceAdjoint[1] * dfForeign / dfDomestic;
    final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency (currency 1) exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency1(), deltaSpot * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * sign);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot+PV
    currencyExposure[1] = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), -deltaSpot * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * spot * sign
        + price);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    Validate.isTrue(curves instanceof YieldCurveWithBlackForexTermStructureBundle, "Smile delta data bundle required");
    return currencyExposure((ForexOptionVanilla) instrument, (YieldCurveWithBlackForexTermStructureBundle) curves);
  }

  /**
   * Computes the relative delta of the Forex option. The relative delta is the amount in the foreign currency equivalent to the option up to the first order divided by the option notional.
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The delta.
   */
  public double deltaRelative(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black, final boolean directQuote) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    final double deltaDirect = BlackFormulaRepository.delta(forward, optionForex.getStrike(), optionForex.getTimeToExpiry(), volatility, optionForex.isCall()) * dfForeign * sign;
    if (directQuote) {
      return deltaDirect;
    }
    final double deltaReverse = -deltaDirect * spot * spot;
    return deltaReverse;
  }

  /**
   * Computes the relative delta of the Forex option multiplied by the spot rate.
   * The relative delta is the amount in the foreign currency equivalent to the option up to the first order divided by the option notional.
   * The reason to multiply by the spot rate is to be able to compute the change of value for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The delta.
   */
  public double deltaRelativeSpot(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black, final boolean directQuote) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    final double deltaDirect = BlackFormulaRepository.delta(forward, optionForex.getStrike(), optionForex.getTimeToExpiry(), volatility, optionForex.isCall()) * dfForeign * sign;
    if (directQuote) {
      return deltaDirect * spot;
    }
    final double deltaReverse = -deltaDirect * spot;
    return deltaReverse;

  }

  /**
   * Computes the relative gamma of the Forex option.
   * The relative gamma is the second order derivative of the pv divided by the option notional.
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The gamma.
   */
  public double gammaRelative(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black, final boolean directQuote) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    final double gammaDirect = BlackFormulaRepository.gamma(forward, optionForex.getStrike(), optionForex.getTimeToExpiry(), volatility) * (dfForeign * dfForeign) / dfDomestic * sign;
    if (directQuote) {
      return gammaDirect;
    }
    final double deltaDirect = BlackFormulaRepository.delta(forward, optionForex.getStrike(), optionForex.getTimeToExpiry(), volatility, optionForex.isCall()) * dfForeign * sign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot * spot;
    return gamma;
  }

  /**
   * Computes the relative gamma of the Forex option multiplied by the spot rate.
   * The relative gamma is the second oder derivative of the pv relative to the option notional.
   * The reason to multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The gamma.
   */
  public double gammaRelativeSpot(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black, final boolean directQuote) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    final double gammaDirect = BlackFormulaRepository.gamma(forward, optionForex.getStrike(), optionForex.getTimeToExpiry(), volatility) * (dfForeign * dfForeign) / dfDomestic * sign;
    if (directQuote) {
      return gammaDirect * spot;
    }
    final double deltaDirect = BlackFormulaRepository.delta(forward, optionForex.getStrike(), optionForex.getTimeToExpiry(), volatility, optionForex.isCall()) * dfForeign * sign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot;
    return gamma;
  }

  /**
   * Computes the gamma of the Forex option. The gamma is the second order derivative of the pv.
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The gamma.
   */
  public CurrencyAmount gamma(final ForexOptionVanilla optionForex, final YieldCurveBundle black, final boolean directQuote) {
    ArgumentChecker.notNull(black, "Curves");
    ArgumentChecker.isTrue(black instanceof YieldCurveWithBlackForexTermStructureBundle, "Yield curve bundle should contain smile data");
    final YieldCurveWithBlackForexTermStructureBundle smile = (YieldCurveWithBlackForexTermStructureBundle) black;
    final double gammaRelative = gammaRelative(optionForex, smile, directQuote);
    return CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), gammaRelative * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the gamma of the Forex option multiplied by the spot rate. The gamma is the second order derivative of the pv.
   * The reason to multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The gamma.
   */
  public CurrencyAmount gammaSpot(final ForexOptionVanilla optionForex, final YieldCurveBundle black, final boolean directQuote) {
    ArgumentChecker.notNull(black, "Curves");
    ArgumentChecker.isTrue(black instanceof YieldCurveWithBlackForexTermStructureBundle, "Yield curve bundle should contain smile data");
    final YieldCurveWithBlackForexTermStructureBundle smile = (YieldCurveWithBlackForexTermStructureBundle) black;
    final double gammaRelativeSpot = gammaRelativeSpot(optionForex, smile, directQuote);
    return CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), gammaRelativeSpot * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the Theta (derivative with respect to the time) using the forward driftless theta in the Black formula. The theta is not scaled.
   * Reference on driftless theta: The complete guide to Option Pricing Formula (2007), E. G. Haug, Mc Graw Hill, p. 67, equation (2.43)
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @return The theta. In the same currency as present value.
   */
  public CurrencyAmount thetaTheoretical(final ForexOptionVanilla optionForex, final YieldCurveBundle black) {
    ArgumentChecker.notNull(black, "Curves");
    ArgumentChecker.isTrue(black instanceof YieldCurveWithBlackForexTermStructureBundle, "Yield curve bundle should contain smile data");
    final YieldCurveWithBlackForexTermStructureBundle smile = (YieldCurveWithBlackForexTermStructureBundle) black;
    final double dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = smile.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    final double theta = BlackFormulaRepository.driftlessTheta(forward, optionForex.getStrike(), optionForex.getTimeToExpiry(), volatility) * sign
        * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
    return CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), theta);
  }

  /**
   * Computes the forward exchange rate associated to the Forex option (1 Cyy1 = fwd Cyy2).
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @return The forward rate.
   */
  public double forwardForexRate(final ForexOptionVanilla optionForex, final YieldCurveBundle black) {
    final ForexDiscountingMethod methodForex = ForexDiscountingMethod.getInstance();
    return methodForex.forwardForexRate(optionForex.getUnderlyingForex(), black);
  }

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility on the forward (and on the curves) is not taken into account. It is the curve
   * sensitivity in the Black model where the volatility is suppose to be constant for curve and forward changes.
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    final String domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
    final String foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
    // Forward sweep
    final double dfDomestic = black.getCurve(domesticCurveName).getDiscountFactor(payTime);
    final double dfForeign = black.getCurve(foreignCurveName).getDiscountFactor(payTime);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(optionForex, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double forwardBar = priceAdjoint[1] * dfDomestic * priceBar;
    final double dfForeignBar = spot / dfDomestic * forwardBar;
    final double dfDomesticBar = -spot / (dfDomestic * dfDomestic) * dfForeign * forwardBar + priceAdjoint[0] * priceBar;
    final double rForeignBar = -payTime * dfForeign * dfForeignBar;
    final double rDomesticBar = -payTime * dfDomestic * dfDomesticBar;
    // Sensitivity object
    final double factor = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0);
    final List<DoublesPair> listForeign = new ArrayList<>();
    listForeign.add(DoublesPair.of(payTime, rForeignBar * factor));
    final Map<String, List<DoublesPair>> resultForeignMap = new HashMap<>();
    resultForeignMap.put(foreignCurveName, listForeign);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultForeignMap);
    final List<DoublesPair> listDomestic = new ArrayList<>();
    listDomestic.add(DoublesPair.of(payTime, rDomesticBar * factor));
    final Map<String, List<DoublesPair>> resultDomesticMap = new HashMap<>();
    resultDomesticMap.put(domesticCurveName, listDomestic);
    result = result.plus(new InterestRateCurveSensitivity(resultDomesticMap));
    return MultipleCurrencyInterestRateCurveSensitivity.of(optionForex.getUnderlyingForex().getCurrency2(), result);
  }

  /**
   * Present value curve sensitivity with a generic instrument as argument.
   * @param instrument A vanilla Forex option.
   * @param black The curve and Black data.
   * @return The curve sensitivity.
   */
  @Override
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle black) {
    Validate.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    Validate.isTrue(black instanceof YieldCurveWithBlackForexTermStructureBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexOptionVanilla) instrument, (YieldCurveWithBlackForexTermStructureBundle) black);
  }

  /**
   * Computes the volatility sensitivity of the vanilla option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double df = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime())
        / df;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(optionForex, dataBlack);
    final double volatilitySensitivityValue = priceAdjoint[2] * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0);
    final DoublesPair point = DoublesPair.of(optionForex.getTimeToExpiry(),
        (optionForex.getCurrency1() == black.getCurrencyPair().getFirst()) ? optionForex.getStrike() : 1.0 / optionForex.getStrike());
    // Implementation note: The strike should be in the same currency order as the input data.
    final SurfaceValue result = SurfaceValue.from(point, volatilitySensitivityValue);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(optionForex.getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingForex()
        .getCurrency2(), result);
    return sensi;
  }

  /**
   * Computes the present value volatility sensitivity with a generic instrument as argument.
   * @param instrument A vanilla Forex option.
   * @param black The curve and Black data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final InstrumentDerivative instrument, final YieldCurveBundle black) {
    Validate.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    Validate.isTrue(black instanceof YieldCurveWithBlackForexTermStructureBundle, "Smile delta data bundle required");
    return presentValueBlackVolatilitySensitivity((ForexOptionVanilla) instrument, (YieldCurveWithBlackForexTermStructureBundle) black);
  }

  /**
   * Computes the volatility sensitivity with respect to the parameters defining the volatility curve.
   * @param optionForex The Forex option.
   * @param black The curve and Black data.
   * @return The volatility parameters sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  //TODO: REVIEW: should a new object with the sensitivity be created?
  public double[] presentValueBlackVolatilityNodeSensitivity(final ForexOptionVanilla optionForex, final YieldCurveWithBlackForexTermStructureBundle black) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(black, "Smile");
    Validate.isTrue(black.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final int nbParameters = black.getVolatilityModel().getVolatilityCurve().size();
    final double df = black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = black.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * black.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime())
        / df;
    final double volatility = black.getVolatilityModel().getVolatility(optionForex.getTimeToExpiry());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(optionForex, dataBlack);
    final double volatilitySensitivityValue = priceAdjoint[2] * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0);
    final Double[] parameterSensitivity = black.getVolatilityModel().getVolatilityTimeSensitivity(optionForex.getTimeToExpiry());
    final double[] vega = new double[nbParameters];
    for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
      vega[loopparam] = parameterSensitivity[loopparam] * volatilitySensitivityValue;
    }
    return vega;
  }

}
