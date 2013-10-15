/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureVannaVolgaDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for vanilla Forex option transactions with Vanna-Volga method.
 * <p>Reference: The vanna-volga method for implied volatilities (2007), A. Castagna and F. Mercurio, Risk, 106-111, January 2007.
 * <p>OG implementation: Vanna-volga method for Forex options, version 1.0, June 2012.
 * <p>The reference volatility used for Black computation is the second volatility (usually corresponding to the ATM strike).
 * @deprecated Use {@link com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaVannaVolgaMethod}
 */
@Deprecated
public final class ForexOptionVanillaVannaVolgaMethod implements ForexPricingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexOptionVanillaVannaVolgaMethod INSTANCE = new ForexOptionVanillaVannaVolgaMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexOptionVanillaVannaVolgaMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexOptionVanillaVannaVolgaMethod() {
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Computes the present value of the vanilla option with the Black function and a volatility from a volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The present value. The value is in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionVanilla optionForex, final SmileDeltaTermStructureVannaVolgaDataBundle smile) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = smile.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final SmileDeltaParameters smileAtTime = smile.getSmile(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getTimeToExpiry());
    final double[] strikesVV = smileAtTime.getStrike(forward);
    final double[] volVV = smileAtTime.getVolatility();
    final double volATM = volVV[1];
    final double[] priceVVATM = new double[3];
    final double[] priceVVsmile = new double[3];
    final BlackFunctionData dataBlackATM = new BlackFunctionData(forward, dfDomestic, volATM);
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) { // Implementation note: The adjustment for K2 is 0
      final BlackFunctionData dataBlackSmile = new BlackFunctionData(forward, dfDomestic, volVV[loopvv]);
      final EuropeanVanillaOption optionVV = new EuropeanVanillaOption(strikesVV[loopvv], optionForex.getTimeToExpiry(), true);
      priceVVATM[loopvv] = BLACK_FUNCTION.getPriceFunction(optionVV).evaluate(dataBlackATM);
      priceVVsmile[loopvv] = BLACK_FUNCTION.getPriceFunction(optionVV).evaluate(dataBlackSmile);
    }
    final double priceFlat = BLACK_FUNCTION.getPriceFunction(optionForex).evaluate(dataBlackATM);
    final double[] x = vannaVolgaWeights(optionForex, forward, dfDomestic, strikesVV, volVV);
    double price = priceFlat;
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) {
      price += x[loopvv] * (priceVVsmile[loopvv] - priceVVATM[loopvv]);
    }
    price *= Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount pvCurrency = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), price);
    return MultipleCurrencyAmount.of(pvCurrency);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureVannaVolgaDataBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionVanilla) instrument, (SmileDeltaTermStructureVannaVolgaDataBundle) curves);
  }

  /**
   * Computes the currency exposure of the vanilla option with the Black function and a volatility from a volatility surface. The exposure is computed in both option currencies.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionVanilla optionForex, final SmileDeltaTermStructureVannaVolgaDataBundle smile) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = smile.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final SmileDeltaParameters smileAtTime = smile.getSmile(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getTimeToExpiry());
    final double[] strikesVV = smileAtTime.getStrike(forward);
    final double[] volVV = smileAtTime.getVolatility();
    final double volATM = volVV[1];
    final double[][] priceVVATM = new double[3][];
    final double[][] priceVVsmile = new double[3][];
    final BlackFunctionData dataBlackATM = new BlackFunctionData(forward, dfDomestic, volATM);
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) { // Implementation note: The adjustment for K2 is 0
      final BlackFunctionData dataBlackSmile = new BlackFunctionData(forward, dfDomestic, volVV[loopvv]);
      final EuropeanVanillaOption optionVV = new EuropeanVanillaOption(strikesVV[loopvv], optionForex.getTimeToExpiry(), true);
      priceVVATM[loopvv] = BLACK_FUNCTION.getPriceAdjoint(optionVV, dataBlackATM);
      priceVVsmile[loopvv] = BLACK_FUNCTION.getPriceAdjoint(optionVV, dataBlackSmile);
    }
    final double[] priceFlat = BLACK_FUNCTION.getPriceAdjoint(optionForex, dataBlackATM);
    final double[] x = vannaVolgaWeights(optionForex, forward, dfDomestic, strikesVV, volVV);
    double price = priceFlat[0];
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) {
      price += x[loopvv] * (priceVVsmile[loopvv][0] - priceVVATM[loopvv][0]);
    }
    price *= Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0);
    double deltaSpot = priceFlat[1];
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) {
      deltaSpot += x[loopvv] * (priceVVsmile[loopvv][1] - priceVVATM[loopvv][1]);
    }
    deltaSpot *= dfForeign / dfDomestic;
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
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
    ArgumentChecker.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureVannaVolgaDataBundle, "Smile delta data bundle required");
    return currencyExposure((ForexOptionVanilla) instrument, (SmileDeltaTermStructureVannaVolgaDataBundle) curves);
  }

  /**
   * Computes the volatility sensitivity of the vanilla option to the reference volatilities.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionVanilla optionForex, final SmileDeltaTermStructureVannaVolgaDataBundle smile) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = smile.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final SmileDeltaParameters smileAtTime = smile.getSmile(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getTimeToExpiry());
    final double[] strikesVV = smileAtTime.getStrike(forward);
    final double[] volVV = smileAtTime.getVolatility();
    final double volATM = volVV[1];
    final double[] priceVVATM = new double[3];
    final double[] priceVVsmile = new double[3];
    final double[] vegaSmile = new double[3];
    final BlackFunctionData dataBlackATM = new BlackFunctionData(forward, dfDomestic, volATM);
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) { // Implementation note: The adjustment for K2 is 0
      final BlackFunctionData dataBlackSmile = new BlackFunctionData(forward, dfDomestic, volVV[loopvv]);
      final EuropeanVanillaOption optionVV = new EuropeanVanillaOption(strikesVV[loopvv], optionForex.getTimeToExpiry(), true);
      priceVVATM[loopvv] = BLACK_FUNCTION.getPriceFunction(optionVV).evaluate(dataBlackATM);
      priceVVsmile[loopvv] = BLACK_FUNCTION.getPriceFunction(optionVV).evaluate(dataBlackSmile);
      vegaSmile[loopvv] = BLACK_FUNCTION.getVegaFunction(optionVV).evaluate(dataBlackSmile);
    }
    //final double priceFlat = BLACK_FUNCTION.getPriceFunction(optionForex).evaluate(dataBlackATM);
    final double[] vega = new double[3];
    final double[] x = vannaVolgaWeights(optionForex, forward, dfDomestic, strikesVV, volVV, vega);
    //double price = priceFlat;
    //for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) {
    //  price += x[loopvv] * (priceVVsmile[loopvv] - priceVVATM[loopvv]);
    //}
    //price *= Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0);
    final double[] vegaReference = new double[3];
    vegaReference[0] = x[0] * vegaSmile[0];
    vegaReference[2] = x[2] * vegaSmile[2];
    vegaReference[1] = vega[1] - x[0] * vega[0] - x[2] * vega[2];
    final SurfaceValue result = new SurfaceValue();
    for (int loopvv = 0; loopvv < 3; loopvv++) {
      final DoublesPair point = DoublesPair.of(optionForex.getTimeToExpiry(), strikesVV[loopvv]);
      result.add(point, vegaReference[loopvv] * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0));
    }
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(optionForex.getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingForex()
        .getCurrency2(), result);
    // TODO: Review when the currency order is not in the standard order.
    return sensi;
  }

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility and the weights on the forward (and on the curves) is not taken into account.
   * @param optionForex The Forex option.
   * @param smile The smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexOptionVanilla optionForex, final SmileDeltaTermStructureVannaVolgaDataBundle smile) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final SmileDeltaParameters smileAtTime = smile.getSmile(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getTimeToExpiry());
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    final String domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
    final String foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
    // Forward sweep
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(payTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(payTime);
    final double spot = smile.getFxRates().getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double[] strikesVV = smileAtTime.getStrike(forward);
    final double[] volVV = smileAtTime.getVolatility();
    final double volATM = volVV[1];
    final double[][] priceVVAdjATM = new double[3][];
    final double[][] priceVVAdjsmile = new double[3][];
    final BlackFunctionData dataBlackATM = new BlackFunctionData(forward, dfDomestic, volATM);
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) { // Implementation note: The adjustment for K2 is 0
      final BlackFunctionData dataBlackSmile = new BlackFunctionData(forward, dfDomestic, volVV[loopvv]);
      final EuropeanVanillaOption optionVV = new EuropeanVanillaOption(strikesVV[loopvv], optionForex.getTimeToExpiry(), true);
      priceVVAdjATM[loopvv] = BLACK_FUNCTION.getPriceAdjoint(optionVV, dataBlackATM);
      priceVVAdjsmile[loopvv] = BLACK_FUNCTION.getPriceAdjoint(optionVV, dataBlackSmile);
    }
    final double[] priceFlat = BLACK_FUNCTION.getPriceAdjoint(optionForex, dataBlackATM);
    final double[] x = vannaVolgaWeights(optionForex, forward, dfDomestic, strikesVV, volVV);
    final double factor = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (optionForex.isLong() ? 1.0 : -1.0);
    //    double pv = priceFlat[0];
    //    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) {
    //      pv += x[loopvv] * (priceVVAdjsmile[loopvv][0] - priceVVAdjATM[loopvv][0]);
    //    }
    //    pv *= factor;
    // Backward sweep
    final double pvBar = 1.0;
    final double[] priceVVATMBar = new double[3];
    final double[] priceVVsmileBar = new double[3];
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) {
      priceVVATMBar[loopvv] = -x[loopvv] * factor * pvBar;
      priceVVsmileBar[loopvv] = x[loopvv] * factor * pvBar;
    }
    final double priceFlatBar = factor * pvBar;
    double forwardBar = priceFlat[1] * priceFlatBar;
    double dfDomesticBar = priceFlat[0] / dfDomestic * priceFlatBar;
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) {
      forwardBar += priceVVAdjATM[loopvv][1] * priceVVATMBar[loopvv];
      forwardBar += priceVVAdjsmile[loopvv][1] * priceVVsmileBar[loopvv];
      dfDomesticBar += priceVVAdjATM[loopvv][0] / dfDomestic * priceVVATMBar[loopvv];
      dfDomesticBar += priceVVAdjsmile[loopvv][0] / dfDomestic * priceVVsmileBar[loopvv];
    }
    dfDomesticBar += -spot * dfForeign / (dfDomestic * dfDomestic) * forwardBar;
    final double dfForeignBar = spot / dfDomestic * forwardBar;
    final double rForeignBar = -payTime * dfForeign * dfForeignBar;
    final double rDomesticBar = -payTime * dfDomestic * dfDomesticBar;
    // Sensitivity object
    final List<DoublesPair> listForeign = new ArrayList<>();
    listForeign.add(DoublesPair.of(payTime, rForeignBar));
    final Map<String, List<DoublesPair>> resultForeignMap = new HashMap<>();
    resultForeignMap.put(foreignCurveName, listForeign);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultForeignMap);
    final List<DoublesPair> listDomestic = new ArrayList<>();
    listDomestic.add(DoublesPair.of(payTime, rDomesticBar));
    final Map<String, List<DoublesPair>> resultDomesticMap = new HashMap<>();
    resultDomesticMap.put(domesticCurveName, listDomestic);
    result = result.plus(new InterestRateCurveSensitivity(resultDomesticMap));
    return MultipleCurrencyInterestRateCurveSensitivity.of(optionForex.getUnderlyingForex().getCurrency2(), result);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureVannaVolgaDataBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexOptionVanilla) instrument, (SmileDeltaTermStructureVannaVolgaDataBundle) curves);
  }

  /**
   * Computes the weights used for adjustment in the vanna-volga method.
   * The weight for the second adjustment (corresponding to ATM strike) is not computed as the adjustment itself is 0 for that strike in our implementation.
   * @param optionForex The option.
   * @param forward The forward FX rate.
   * @param dfDomestic The discounting factor to the payment date in the domestic currency.
   * @param strikesReference The reference strikes used for the vanna-volga method.
   * @param volatilitiesReference The volatilities at the reference strikes.
   * @return The weights.
   */
  public double[] vannaVolgaWeights(final ForexOptionVanilla optionForex, final double forward, final double dfDomestic, final double[] strikesReference, final double[] volatilitiesReference) {
    return vannaVolgaWeights(optionForex, forward, dfDomestic, strikesReference, volatilitiesReference, new double[3]);
  }

  /**
   * Computes the weights used for adjustment in the vanna-volga method.
   * The weight for the second adjustment (corresponding to ATM strike) is not computed as the adjustment itself is 0 for that strike in our implementation.
   * @param optionForex The option.
   * @param forward The forward FX rate.
   * @param dfDomestic The discounting factor to the payment date in the domestic currency.
   * @param strikesReference The reference strikes used for the vanna-volga method.
   * @param volatilitiesReference The volatilities at the reference strikes.
   * @param vega The vega using the base volatility at the reference points (index 0 and 2) and at the strike (index 1). The array is changed with the method call.
   * @return The weights.
   */
  public double[] vannaVolgaWeights(final ForexOptionVanilla optionForex, final double forward, final double dfDomestic, final double[] strikesReference,
      final double[] volatilitiesReference, final double[] vega) {
    final double strike = optionForex.getStrike();
    final double volATM = volatilitiesReference[1]; // The reference volatility is the "middle" one, which is often ATM.
    final BlackFunctionData dataBlackATM = new BlackFunctionData(forward, dfDomestic, volATM);
    for (int loopvv = 0; loopvv < 3; loopvv = loopvv + 2) { // Implementation note: The adjustment for K2 is 0
      final EuropeanVanillaOption optionVV = new EuropeanVanillaOption(strikesReference[loopvv], optionForex.getTimeToExpiry(), true);
      vega[loopvv] = BLACK_FUNCTION.getVegaFunction(optionVV).evaluate(dataBlackATM);
    }
    final double vegaFlat = BLACK_FUNCTION.getVegaFunction(optionForex).evaluate(dataBlackATM);
    vega[1] = vegaFlat;
    final double lnk21 = Math.log(strikesReference[1] / strikesReference[0]);
    final double lnk31 = Math.log(strikesReference[2] / strikesReference[0]);
    final double lnk32 = Math.log(strikesReference[2] / strikesReference[1]);
    final double[] lnk = new double[3];
    for (int loopvv = 0; loopvv < 3; loopvv++) {
      lnk[loopvv] = Math.log(strikesReference[loopvv] / strike);
    }
    final double[] x = new double[3];
    x[0] = vegaFlat * lnk[1] * lnk[2] / (vega[0] * lnk21 * lnk31);
    x[2] = vegaFlat * lnk[0] * lnk[1] / (vega[2] * lnk31 * lnk32);
    return x;
  }

}
