/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearWithConvexityProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for inflation Year on Year cap\floor. The price is computed by index estimation, discounting and using a convexity adjustment.
 * See note "Inflation convexity adjustment" by Arroub Zine-eddine for details.
 */
public final class CapFloorInflationYearOnYearInterpolationConvexityAdjustmentMethod {

  /**
   * The method unique instance.
   */
  private static final CapFloorInflationYearOnYearInterpolationConvexityAdjustmentMethod INSTANCE = new CapFloorInflationYearOnYearInterpolationConvexityAdjustmentMethod();

  /**
   * Private constructor.
   */
  private CapFloorInflationYearOnYearInterpolationConvexityAdjustmentMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorInflationYearOnYearInterpolationConvexityAdjustmentMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final NormalPriceFunction NORMAL_FUNCTION = new NormalPriceFunction();

  /**
   * The convexity adjustment function used in the pricing.
   */
  private static final InflationMarketModelConvexityAdjustmentForCapFloor CONVEXITY_ADJUSTMENT_FUNCTION = new InflationMarketModelConvexityAdjustmentForCapFloor();

  /**
   * Computes the net amount.
   * @param cap The caplet/floorlet.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount netAmount(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearWithConvexityProviderInterface black) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(black, "Black provider");
    final double timeToMaturity = cap.getReferenceEndTime()[1] - cap.getLastKnownFixingTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), timeToMaturity, cap.isCap());
    final double priceIndexStart0 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceStartTime()[0]);
    final double priceIndexStart1 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceStartTime()[1]);
    final double priceIndexStart = cap.getWeightStart() * priceIndexStart0 + (1 - cap.getWeightStart()) * priceIndexStart1;
    final double priceIndexEnd0 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceEndTime()[0]);
    final double priceIndexEnd1 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceEndTime()[1]);
    final double priceIndexEnd = cap.getWeightEnd() * priceIndexEnd0 + (1 - cap.getWeightEnd()) * priceIndexEnd1;
    final double convexityAdjustment = CONVEXITY_ADJUSTMENT_FUNCTION.getYearOnYearConvexityAdjustment(cap, black);
    final double forward = priceIndexEnd / priceIndexStart * convexityAdjustment - 1;
    final double volatility = black.getBlackParameters().getVolatility(cap.getReferenceEndTime()[1], cap.getStrike());
    final NormalFunctionData dataBlack = new NormalFunctionData(forward, 1.0, volatility);
    final Function1D<NormalFunctionData, Double> func = NORMAL_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    return MultipleCurrencyAmount.of(cap.getCurrency(), price);
  }

  /**
   * Computes the present value.
   * @param cap The caplet/floorlet.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearWithConvexityProviderInterface black) {
    final MultipleCurrencyAmount nonDiscountedPresentValue = netAmount(cap, black);
    final double df = black.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    return nonDiscountedPresentValue.multipliedBy(df);
  }

  /**
   * Computes the present value.
   * @param instrument The instrument.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final BlackSmileCapInflationYearOnYearWithConvexityProviderInterface black) {
    ArgumentChecker.isTrue(instrument instanceof CapFloorInflationYearOnYearInterpolation, "Inflation Year on Year Cap/floor");
    return presentValue((CapFloorInflationYearOnYearInterpolation) instrument, black);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cap/floor in the Black model.
   * No smile impact is taken into account; equivalent to a sticky strike smile description.
   * @param cap The caplet/floorlet.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final CapFloorInflationYearOnYearInterpolation cap,
      final BlackSmileCapInflationYearOnYearWithConvexityProviderInterface black) {
    ArgumentChecker.notNull(cap, "The cap/floor should not be null");
    ArgumentChecker.notNull(black, "Black provider");
    final InflationProviderInterface inflation = black.getInflationProvider();
    final double timeToMaturity = cap.getReferenceEndTime()[1] - cap.getLastKnownFixingTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), timeToMaturity, cap.isCap());
    final double priceIndexStart0 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceStartTime()[0]);
    final double priceIndexStart1 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceStartTime()[1]);
    final double priceIndexStart = cap.getWeightStart() * priceIndexStart0 + (1 - cap.getWeightStart()) * priceIndexStart1;
    final double priceIndexEnd0 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceEndTime()[0]);
    final double priceIndexEnd1 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceEndTime()[1]);
    final double priceIndexEnd = cap.getWeightEnd() * priceIndexEnd0 + (1 - cap.getWeightEnd()) * priceIndexEnd1;
    final double convexityAdjustment = CONVEXITY_ADJUSTMENT_FUNCTION.getYearOnYearConvexityAdjustment(cap, black);
    final double forward = priceIndexEnd / priceIndexStart * convexityAdjustment - 1;
    final double df = black.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final Map<String, List<DoublesPair>> resultMapPrice = new HashMap<>();
    final List<DoublesPair> listPrice = new ArrayList<>();
    listPrice.add(DoublesPair.of(cap.getReferenceEndTime()[0], cap.getWeightEnd() / priceIndexStart * convexityAdjustment));
    listPrice.add(DoublesPair.of(cap.getReferenceEndTime()[1], (1 - cap.getWeightEnd()) / priceIndexStart * convexityAdjustment));
    listPrice.add(DoublesPair.of(cap.getReferenceStartTime()[0], -cap.getWeightStart() * priceIndexEnd / (priceIndexStart * priceIndexStart) * convexityAdjustment));
    listPrice.add(DoublesPair.of(cap.getReferenceStartTime()[1], -(1 - cap.getWeightStart()) * priceIndexEnd / (priceIndexStart * priceIndexStart) * convexityAdjustment));
    resultMapPrice.put(inflation.getName(cap.getPriceIndex()), listPrice);
    final InflationSensitivity forwardDi = InflationSensitivity.ofPriceIndex(resultMapPrice);
    final double dfDr = -cap.getPaymentTime() * df;
    final double volatility = black.getBlackParameters().getVolatility(cap.getReferenceEndTime()[1], cap.getStrike());
    final NormalFunctionData dataBlack = new NormalFunctionData(forward, 1.0, volatility);
    final double[] priceDerivatives = new double[3];
    final double bsAdjoint = NORMAL_FUNCTION.getPriceAdjoint(option, dataBlack, priceDerivatives);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(cap.getPaymentTime(), dfDr));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    resultMap.put(inflation.getName(cap.getCurrency()), list);
    InflationSensitivity result = InflationSensitivity.ofYieldDiscounting(resultMap);
    result = result.multipliedBy(bsAdjoint);
    result = result.plus(forwardDi.multipliedBy(df * priceDerivatives[0]));
    result = result.multipliedBy(cap.getNotional() * cap.getPaymentYearFraction());
    return MultipleCurrencyInflationSensitivity.of(cap.getCurrency(), result);
  }

}
