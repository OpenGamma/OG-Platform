/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Triple;

/**
 * Pricing method for vanilla Forex option transactions with Black function and a volatility provider.
 */
public class ForexOptionVanillaMethod implements ForexPricingMethod {

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  public MultipleCurrencyAmount presentValue(final ForexOptionVanilla optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    double df = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    double spot = smile.getSpot();
    double forward = spot * smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime()) / df;
    double volatility = smile.getSmile().getVolatility(new Triple<Double, Double, Double>(optionForex.getTimeToExpiry(), optionForex.getStrike(), forward));
    BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(optionForex);
    double price = func.evaluate(dataBlack) * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
    CurrencyAmount priceCurrency = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final ForexDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionVanilla) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  public MultipleCurrencyAmount currencyExposure(final ForexOptionVanilla optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    double dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    double dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    double spot = smile.getSpot();
    double forward = spot * dfForeign / dfDomestic;
    double volatility = smile.getSmile().getVolatility(new Triple<Double, Double, Double>(optionForex.getTimeToExpiry(), optionForex.getStrike(), forward));
    BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(optionForex, dataBlack);
    double price = priceAdjoint[0] * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
    double deltaSpot = priceAdjoint[1] * dfForeign / dfDomestic;
    CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency (currency 1) exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency1(), deltaSpot * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()));
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot+PV
    currencyExposure[1] = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(),
        -deltaSpot * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()) * smile.getSpot() + price);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final ForexDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionVanilla, "Vanilla Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return currencyExposure((ForexOptionVanilla) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

}
