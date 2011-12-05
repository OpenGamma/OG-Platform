/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.surface.SurfaceValue;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex non-deliverable option transactions with Black function and a smile.
 */
public final class ForexNonDeliverableOptionBlackMethod implements ForexPricingMethod {

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
   * Computes the present value of Forex non-deliverable option with the Black function and a volatility surface.
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

  @Override
  public MultipleCurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexNonDeliverableOption, "Forex non-deliverable option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValue((ForexNonDeliverableOption) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the currency exposure of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexNonDeliverableOption optionForex, final SmileDeltaTermStructureDataBundle smile) {
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
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    final double price = priceAdjoint[0] * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1()) * sign;
    final double deltaSpot = priceAdjoint[1] * dfNonDelivery / dfDelivery;
    final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency (currency 1) exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(optionForex.getCurrency1(), deltaSpot * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1()) * sign);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot+PV
    currencyExposure[1] = CurrencyAmount.of(optionForex.getCurrency2(), -deltaSpot * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1()) * spot * sign + price);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexNonDeliverableOption, "Forex non-deliverable option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return currencyExposure((ForexNonDeliverableOption) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the forward exchange rate associated to the NOF (1 Cyy2 = fwd Cyy1).
   * @param ndo The non-deliverable option.
   * @param smile The curve and smile data.
   * @return The forward rate.
   */
  public double forwardForexRate(final ForexNonDeliverableOption ndo, final SmileDeltaTermStructureDataBundle smile) {
    ForexNonDeliverableForwardDiscountingMethod method = ForexNonDeliverableForwardDiscountingMethod.getInstance();
    return method.forwardForexRate(ndo.getUnderlyingNDF(), smile);
  }

  /**
   * Computes the present value curve sensitivities of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The curve sensitivities.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexNonDeliverableOption optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final String deliveryCurveName = optionForex.getUnderlyingNDF().getDiscountingCurve2Name();
    final String nonDeliveryCurveName = optionForex.getUnderlyingNDF().getDiscountingCurve1Name();
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    // Forward sweep
    final double dfDelivery = smile.getCurve(deliveryCurveName).getDiscountFactor(paymentTime);
    final double dfNonDelivery = smile.getCurve(nonDeliveryCurveName).getDiscountFactor(paymentTime);
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, !optionForex.isCall());
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = sign;
    final double forwardBar = priceAdjoint[1] * dfDelivery * priceBar;
    final double dfNonDeliveryBar = spot / dfDelivery * forwardBar;
    final double dfDeliveryBar = -spot / (dfDelivery * dfDelivery) * dfNonDelivery * forwardBar + priceAdjoint[0] * priceBar;
    final double rNonDeliveryBar = -paymentTime * dfNonDelivery * dfNonDeliveryBar;
    final double rDeliveryBar = -paymentTime * dfDelivery * dfDeliveryBar;
    // Sensitivity object
    final List<DoublesPair> listNonDelivery = new ArrayList<DoublesPair>();
    listNonDelivery.add(new DoublesPair(paymentTime, rNonDeliveryBar * optionForex.getUnderlyingNDF().getNotionalCurrency1()));
    final Map<String, List<DoublesPair>> resultNonDeliveryMap = new HashMap<String, List<DoublesPair>>();
    resultNonDeliveryMap.put(nonDeliveryCurveName, listNonDelivery);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultNonDeliveryMap);
    final List<DoublesPair> listDelivery = new ArrayList<DoublesPair>();
    listDelivery.add(new DoublesPair(paymentTime, rDeliveryBar * optionForex.getUnderlyingNDF().getNotionalCurrency1()));
    final Map<String, List<DoublesPair>> resultDeliveryMap = new HashMap<String, List<DoublesPair>>();
    resultDeliveryMap.put(deliveryCurveName, listDelivery);
    result = result.add(new InterestRateCurveSensitivity(resultDeliveryMap));
    return MultipleCurrencyInterestRateCurveSensitivity.of(optionForex.getCurrency2(), result);
  }

  /**
   * Computes the present value curve sensitivities of Forex non-deliverable option with the Black function and a volatility surface.
   * @param instrument A Forex non-deliverable option.
   * @param curves The volatility and curves description (SmileDeltaTermStructureDataBundle).
   * @return The curve sensitivities.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexNonDeliverableOption, "Forex non-deliverable option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexNonDeliverableOption) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the present value volatility sensitivity (sensitivity to one volatility point) of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The currency exposure
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueVolatilitySensitivity(final ForexNonDeliverableOption optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final String deliveryCurveName = optionForex.getUnderlyingNDF().getDiscountingCurve2Name();
    final String nonDeliveryCurveName = optionForex.getUnderlyingNDF().getDiscountingCurve1Name();
    // Forward sweep
    final double dfDelivery = smile.getCurve(deliveryCurveName).getDiscountFactor(paymentTime);
    final double dfNonDelivery = smile.getCurve(nonDeliveryCurveName).getDiscountFactor(paymentTime);
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDelivery, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, !optionForex.isCall());
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double volatilitySensitivityValue = priceAdjoint[2] * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1()) * (optionForex.isLong() ? 1.0 : -1.0);
    final DoublesPair point = DoublesPair.of(optionForex.getExpiryTime(), (optionForex.getCurrency1() == smile.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    SurfaceValue result = SurfaceValue.from(point, volatilitySensitivityValue);
    //    final Map<DoublesPair, Double> result = new HashMap<DoublesPair, Double>();
    //    result.put(point, volatilitySensitivityValue);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(optionForex.getCurrency1(), optionForex.getCurrency2(), result);
    return sensi;
  }

  /**
   * Computes the present value volatility sensitivity (sensitivity to each point of the volatility input grid) of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The currency exposure
   */
  public PresentValueVolatilityNodeSensitivityDataBundle presentValueVolatilityNodeSensitivity(final ForexNonDeliverableOption optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final String deliveryCurveName = optionForex.getUnderlyingNDF().getDiscountingCurve2Name();
    final String nonDeliveryCurveName = optionForex.getUnderlyingNDF().getDiscountingCurve1Name();
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueVolatilitySensitivity(optionForex, smile); // In ccy2
    final double[][] nodeWeight = new double[smile.getSmile().getNumberExpiration()][smile.getSmile().getNumberStrike()];
    final double dfDelivery = smile.getCurve(deliveryCurveName).getDiscountFactor(paymentTime);
    final double dfNonDelivery = smile.getCurve(nonDeliveryCurveName).getDiscountFactor(paymentTime);
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward, nodeWeight);
    final DoublesPair point = DoublesPair.of(expiryTime, (optionForex.getCurrency1() == smile.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final double[][] vega = new double[smile.getSmile().getNumberExpiration()][smile.getSmile().getNumberStrike()];
    for (int loopexp = 0; loopexp < smile.getSmile().getNumberExpiration(); loopexp++) {
      for (int loopstrike = 0; loopstrike < smile.getSmile().getNumberStrike(); loopstrike++) {
        vega[loopexp][loopstrike] = nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueVolatilityNodeSensitivityDataBundle(optionForex.getCurrency1(), optionForex.getCurrency2(), new DoubleMatrix1D(smile.getSmile().getTimeToExpiration()), new DoubleMatrix1D(
        smile.getSmile().getDeltaFull()), new DoubleMatrix2D(vega));
  }

}
