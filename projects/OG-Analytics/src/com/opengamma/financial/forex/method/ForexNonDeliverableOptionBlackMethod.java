/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for Forex non-deliverable option transactions with Black function and a smile.
 */
public final class ForexNonDeliverableOptionBlackMethod {

  /**
   * The method unique instance.
   */
  private static final ForexNonDeliverableOptionBlackMethod INSTANCE = new ForexNonDeliverableOptionBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexNonDeliverableOptionBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexNonDeliverableOptionBlackMethod() {
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Computes the present value of the non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The present value. The value is in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexNonDeliverableOption optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double dfDelivery = smile.getCurve(optionForex.getUnderlyingNDF().getDiscountingCurve2Name()).getDiscountFactor(paymentTime);
    final double dfNonDelivery = smile.getCurve(optionForex.getUnderlyingNDF().getDiscountingCurve1Name()).getDiscountFactor(paymentTime);
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDelivery, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, !optionForex.isCall());
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1()) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(optionForex.getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

}
