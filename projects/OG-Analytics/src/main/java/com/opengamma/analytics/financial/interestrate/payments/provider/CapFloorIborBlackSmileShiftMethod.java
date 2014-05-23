/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSmileShiftCapProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor with Black model.
 *  No convexity adjustment is done for payment at non-standard dates.
 */
public final class CapFloorIborBlackSmileShiftMethod {

  /**
   * The method unique instance.
   */
  private static final CapFloorIborBlackSmileShiftMethod INSTANCE = new CapFloorIborBlackSmileShiftMethod();

  /**
   * Private constructor.
   */
  private CapFloorIborBlackSmileShiftMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorIborBlackSmileShiftMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Computes the present value.
   * @param cap The caplet/floorlet.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final BlackSmileShiftCapProviderInterface black) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(black, "Black provider");
    final double forward = black.getMulticurveProvider().getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = black.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final double volatility = black.getBlackShiftParameters().getVolatility(cap.getFixingTime(), cap.getStrike());
    final double shift = black.getBlackShiftParameters().getShift(cap.getFixingTime());
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike() + shift, cap.getFixingTime(), cap.isCap());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward + shift, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    return MultipleCurrencyAmount.of(cap.getCurrency(), price);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cap/floor in the Black model.
   * No smile impact is taken into account; equivalent to a sticky strike smile description.
   * @param cap The caplet/floorlet.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CapFloorIbor cap, final BlackSmileShiftCapProviderInterface black) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(black, "Black provider");
    final MulticurveProviderInterface multicurve = black.getMulticurveProvider();
    final double forward = multicurve.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = multicurve.getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final MulticurveSensitivity forwardDr = MulticurveSensitivity.ofForward(multicurve.getName(cap.getIndex()),
        new SimplyCompoundedForwardSensitivity(cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor(), 1.0));
    final double dfDr = -cap.getPaymentTime() * df;
    final double volatility = black.getBlackShiftParameters().getVolatility(cap.getFixingTime(), cap.getStrike());
    final double shift = black.getBlackShiftParameters().getShift(cap.getFixingTime());
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike() + shift, cap.getFixingTime(), cap.isCap());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward + shift, 1.0, volatility);
    final double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(cap.getPaymentTime(), dfDr));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    resultMap.put(multicurve.getName(cap.getCurrency()), list);
    MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    result = result.multipliedBy(bsAdjoint[0]);
    result = result.plus(forwardDr.multipliedBy(df * bsAdjoint[1]));
    result = result.multipliedBy(cap.getNotional() * cap.getPaymentYearFraction());
    return MultipleCurrencyMulticurveSensitivity.of(cap.getCurrency(), result);
  }

}
