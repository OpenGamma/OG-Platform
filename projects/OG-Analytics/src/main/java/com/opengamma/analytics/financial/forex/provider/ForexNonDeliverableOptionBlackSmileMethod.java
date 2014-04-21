/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex non-deliverable option transactions with Black function and a smile.
 */
public final class ForexNonDeliverableOptionBlackSmileMethod {

  /**
   * The method unique instance.
   */
  private static final ForexNonDeliverableOptionBlackSmileMethod INSTANCE = new ForexNonDeliverableOptionBlackSmileMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexNonDeliverableOptionBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexNonDeliverableOptionBlackSmileMethod() {
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Methods.
   */
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();

  /**
   * Computes the present value of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The present value. The value is in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexNonDeliverableOption optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smileMulticurves, "Smile");
    Validate.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double dfDelivery = multicurves.getDiscountFactor(optionForex.getCurrency2(), paymentTime);
    final double dfNonDelivery = multicurves.getDiscountFactor(optionForex.getCurrency1(), paymentTime);
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDelivery, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, !optionForex.isCall());
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1()) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(optionForex.getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  /**
   * Computes the currency exposure of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexNonDeliverableOption optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smileMulticurves, "Smile");
    Validate.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double dfDelivery = multicurves.getDiscountFactor(optionForex.getCurrency2(), paymentTime);
    final double dfNonDelivery = multicurves.getDiscountFactor(optionForex.getCurrency1(), paymentTime);
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward);
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

  /**
   * Computes the forward exchange rate associated to the NOF (1 Cyy2 = fwd Cyy1).
   * @param ndo The non-deliverable option.
   * @param multicurves The multi-curves provider.
   * @return The forward rate.
   */
  public double forwardForexRate(final ForexNonDeliverableOption ndo, final MulticurveProviderInterface multicurves) {
    return METHOD_NDF.forwardForexRate(ndo.getUnderlyingNDF(), multicurves);
  }

  /**
   * Computes the present value curve sensitivities of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The curve sensitivities.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexNonDeliverableOption optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smileMulticurves, "Smile");
    Validate.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double sign = (optionForex.isLong() ? 1.0 : -1.0);
    // Forward sweep
    final double dfDelivery = multicurves.getDiscountFactor(optionForex.getCurrency2(), paymentTime);
    final double dfNonDelivery = multicurves.getDiscountFactor(optionForex.getCurrency1(), paymentTime);
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward);
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
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listNonDelivery = new ArrayList<>();
    listNonDelivery.add(DoublesPair.of(paymentTime, rNonDeliveryBar * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1())));
    resultMap.put(multicurves.getName(optionForex.getCurrency1()), listNonDelivery);
    final List<DoublesPair> listDelivery = new ArrayList<>();
    listDelivery.add(DoublesPair.of(paymentTime, rDeliveryBar * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1())));
    resultMap.put(multicurves.getName(optionForex.getCurrency2()), listDelivery);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(optionForex.getCurrency2(), result);
  }

  /**
   * Computes the present value volatility sensitivity (sensitivity to one volatility point) of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The currency exposure
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexNonDeliverableOption optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smileMulticurves, "Smile");
    Validate.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    // Forward sweep
    final double dfDelivery = multicurves.getDiscountFactor(optionForex.getCurrency2(), paymentTime);
    final double dfNonDelivery = multicurves.getDiscountFactor(optionForex.getCurrency1(), paymentTime);
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDelivery, volatility);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, !optionForex.isCall());
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double volatilitySensitivityValue = priceAdjoint[2] * Math.abs(optionForex.getUnderlyingNDF().getNotionalCurrency1()) * (optionForex.isLong() ? 1.0 : -1.0);
    final DoublesPair point = DoublesPair.of(optionForex.getExpiryTime(), (optionForex.getCurrency1() == smileMulticurves.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final SurfaceValue result = SurfaceValue.from(point, volatilitySensitivityValue);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(optionForex.getCurrency1(), optionForex.getCurrency2(), result);
    return sensi;
  }

  /**
   * Computes the present value volatility sensitivity (sensitivity to each point of the volatility input grid) of Forex non-deliverable option with the Black function and a volatility surface.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The currency exposure
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueVolatilityNodeSensitivity(final ForexNonDeliverableOption optionForex,
      final BlackForexSmileProviderInterface smileMulticurves) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smileMulticurves, "Smile");
    Validate.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double paymentTime = optionForex.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = optionForex.getExpiryTime();
    final double strike = 1.0 / optionForex.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(optionForex, smileMulticurves); // In ccy2
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = smileMulticurves.getVolatility();
    final double dfDelivery = multicurves.getDiscountFactor(optionForex.getCurrency2(), paymentTime);
    final double dfNonDelivery = multicurves.getDiscountFactor(optionForex.getCurrency1(), paymentTime);
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final VolatilityAndBucketedSensitivities volAndSensitivities = smileMulticurves.getVolatilityAndSensitivities(optionForex.getCurrency1(), optionForex.getCurrency2(), expiryTime, strike, forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(expiryTime, (optionForex.getCurrency1() == smileMulticurves.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (int loopexp = 0; loopexp < volatilityModel.getNumberExpiration(); loopexp++) {
      for (int loopstrike = 0; loopstrike < volatilityModel.getNumberStrike(); loopstrike++) {
        vega[loopexp][loopstrike] = nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(optionForex.getCurrency1(), optionForex.getCurrency2(), new DoubleMatrix1D(volatilityModel.getTimeToExpiration()),
        new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

}
