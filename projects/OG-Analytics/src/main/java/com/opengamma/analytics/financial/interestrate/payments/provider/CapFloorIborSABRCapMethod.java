/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.analytics.financial.provider.method.CapFloorIborSABRCapMethodInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor with SABR model.
 *  No convexity adjustment is done for payment at non-standard dates.
 */
public final class CapFloorIborSABRCapMethod implements CapFloorIborSABRCapMethodInterface {

  /**
   * The method unique instance.
   */
  private static final CapFloorIborSABRCapMethod INSTANCE = new CapFloorIborSABRCapMethod();

  /**
   * Private constructor.
   */
  private CapFloorIborSABRCapMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorIborSABRCapMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Computes the present value of a cap/floor in the SABR model.
   * @param cap The cap/floor.
   * @param sabr The SABR cap and multi-curves provider.
   * @return The present value.
   */
  @Override
  public MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(sabr, "SABR cap provider");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = sabr.getMulticurveProvider().getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = sabr.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    // TODO: Improve maturity, using periods?
    final double volatility = sabr.getSABRParameter().getVolatility(cap.getFixingTime(), maturity, cap.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    return MultipleCurrencyAmount.of(cap.getCurrency(), price);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cap/floor in the SABR model.
   * @param cap The cap/floor.
   * @param sabr The SABR cap and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(sabr, "SABR cap provider");
    final MulticurveProviderInterface multicurve = sabr.getMulticurveProvider();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = multicurve.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = multicurve.getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final MulticurveSensitivity forwardDr = MulticurveSensitivity.ofForward(multicurve.getName(cap.getIndex()),
        new SimplyCompoundedForwardSensitivity(cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor(), 1.0));
    final double dfDr = -cap.getPaymentTime() * df;
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    final double[] volatilityAdjoint = sabr.getSABRParameter().getVolatilityAdjoint(cap.getFixingTime(), maturity, cap.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(cap.getPaymentTime(), dfDr));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    resultMap.put(multicurve.getName(cap.getCurrency()), list);
    MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    result = result.multipliedBy(bsAdjoint[0]);
    result = result.plus(forwardDr.multipliedBy(df * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1])));
    result = result.multipliedBy(cap.getNotional() * cap.getPaymentYearFraction());
    return MultipleCurrencyMulticurveSensitivity.of(cap.getCurrency(), result);
  }

  /**
   * Computes the present value SABR sensitivity of a cap/floor in the SABR model.
   * @param cap The cap/floor.
   * @param sabr The SABR cap and multi-curves provider.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(sabr, "SABR cap provider");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = sabr.getMulticurveProvider().getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = sabr.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    final double[] volatilityAdjoint = sabr.getSABRParameter().getVolatilityAdjoint(cap.getFixingTime(), maturity, cap.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final DoublesPair expiryMaturity = DoublesPair.of(cap.getFixingTime(), maturity);
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    sensi.addAlpha(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsAdjoint[2] * volatilityAdjoint[3]);
    sensi.addBeta(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsAdjoint[2] * volatilityAdjoint[4]);
    sensi.addRho(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsAdjoint[2] * volatilityAdjoint[5]);
    sensi.addNu(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsAdjoint[2] * volatilityAdjoint[6]);
    return sensi;
  }

}
