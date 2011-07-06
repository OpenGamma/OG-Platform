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

import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * Pricing method for single barrier Forex option transactions in the Black world.
 */
public class ForexOptionSingleBarrierBlackMethod implements ForexPricingMethod {

  /**
   * The Black function used in the barrier pricing.
   */
  private static final BlackBarrierPriceFunction BLACK_FUNCTION = new BlackBarrierPriceFunction();

  /**
   * Computes the present value for single barrier Forex option in Black model (log-normal spot rate).
   * @param optionForex The Forex option.
   * @param smile The volatility and curves description.
   * @return The present value (in domestic currency).
   */
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
    double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    double volatility = smile.getSmile().getVolatility(new Triple<Double, Double, Double>(optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption().getStrike(), forward));
    double price = BLACK_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateForeign, rateDomestic, volatility);
    price *= Math.abs(foreignAmount) * sign;
    CurrencyAmount priceCurrency = CurrencyAmount.of(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final ForexDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionSingleBarrier, "Single barrier Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionSingleBarrier) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the currency exposure for single barrier Forex option in Black model (log-normal spot rate). The sensitivity of the volatility on the spot
   * is not taken into account. It is the currency exposure in the Black model where the volatility is suppose to be constant for curve and forward changes.
   * @param optionForex The Forex option.
   * @param smile The volatility and curves description.
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionSingleBarrier optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    double rateDomestic = smile.getCurve(optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getInterestRate(payTime);
    double rateForeign = smile.getCurve(optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getInterestRate(payTime);
    double spot = smile.getSpot();
    double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    double volatility = smile.getSmile().getVolatility(new Triple<Double, Double, Double>(optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption().getStrike(), forward));
    double[] priceDerivatives = new double[5];
    double price = BLACK_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateForeign, rateDomestic, volatility, priceDerivatives);
    price *= Math.abs(foreignAmount) * sign;
    double deltaSpot = priceDerivatives[0];
    CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency (currency 1) exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency1(), deltaSpot * Math.abs(foreignAmount) * sign);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot+PV
    currencyExposure[1] = CurrencyAmount.of(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2(),
        -deltaSpot * Math.abs(optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount()) * smile.getSpot() * sign + price);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(ForexDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionSingleBarrier, "Single barrier Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return currencyExposure((ForexOptionSingleBarrier) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility on the forward (and on the curves) is not taken into account. It is the curve
   * sensitivity in the Black model where the volatility is suppose to be constant for curve and forward changes.
   * @param optionForex The Forex option.
   * @param smile The volatility and curves description.
   * @return The curve sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final ForexOptionSingleBarrier optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    String domesticCurveName = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
    String foreignCurveName = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
    double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    double rateDomestic = smile.getCurve(domesticCurveName).getInterestRate(payTime);
    double rateForeign = smile.getCurve(foreignCurveName).getInterestRate(payTime);
    double spot = smile.getSpot();
    // Forward sweep
    double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    double volatility = smile.getSmile().getVolatility(new Triple<Double, Double, Double>(optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption().getStrike(), forward));
    double[] priceDerivatives = new double[5];
    BLACK_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateForeign, rateDomestic, volatility, priceDerivatives);
    // Backward sweep
    double priceBar = 1.0;
    double rForeignBar = priceDerivatives[3] * Math.abs(foreignAmount) * sign * priceBar;
    double rDomesticBar = priceDerivatives[2] * Math.abs(foreignAmount) * sign * priceBar;
    // Sensitivity object
    final List<DoublesPair> listForeign = new ArrayList<DoublesPair>();
    listForeign.add(new DoublesPair(payTime, rForeignBar));
    final Map<String, List<DoublesPair>> resultForeignMap = new HashMap<String, List<DoublesPair>>();
    resultForeignMap.put(foreignCurveName, listForeign);
    PresentValueSensitivity result = new PresentValueSensitivity(resultForeignMap);
    final List<DoublesPair> listDomestic = new ArrayList<DoublesPair>();
    listDomestic.add(new DoublesPair(payTime, rDomesticBar));
    final Map<String, List<DoublesPair>> resultDomesticMap = new HashMap<String, List<DoublesPair>>();
    resultDomesticMap.put(domesticCurveName, listDomestic);
    result = result.add(new PresentValueSensitivity(resultDomesticMap));
    return result;
  }

  /**
   * Present value curve sensitivity with a generic instrument as argument.
   * @param instrument A single barrier Forex option.
   * @param curves The volatility and curves description (SmileDeltaTermStructureDataBundle).
   * @return The curve sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final ForexDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionSingleBarrier, "Single barrier Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexOptionSingleBarrier) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

}
