/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Triple;

/**
 * Pricing method for single barrier Forex option transactions in the Black world.
 */
public class ForexOptionSingleBarrierBlackMethod implements ForexPricingMethod {

  /**
   * The Black function used in the barrier pricing.
   */
  private static final BlackBarrierPriceFunction BLACK_FUNCTION = new BlackBarrierPriceFunction();

  public MultipleCurrencyAmount presentValue(final ForexOptionSingleBarrier optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    double rateDomestic = smile.getCurve(optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getInterestRate(payTime);
    double rateForeign = smile.getCurve(optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getInterestRate(payTime);
    double spot = smile.getSpot();
    double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    double volatility = smile.getSmile().getVolatility(new Triple<Double, Double, Double>(optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption().getStrike(), forward));
    double price = BLACK_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateForeign, rateDomestic, volatility);
    price *= foreignAmount * (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    CurrencyAmount priceCurrency = CurrencyAmount.of(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final ForexDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionSingleBarrier, "Single barrier Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionSingleBarrier) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(ForexDerivative instrument, YieldCurveBundle curves) {
    //TODO Not implemented yet.
    return null;
  }

}
