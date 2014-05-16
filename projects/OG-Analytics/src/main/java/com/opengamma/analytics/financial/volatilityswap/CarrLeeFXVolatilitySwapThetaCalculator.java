/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute theta of forward volatility based on finite difference method
 */
public class CarrLeeFXVolatilitySwapThetaCalculator extends InstrumentDerivativeVisitorAdapter<CarrLeeFXData, Double> {
  private static final CarrLeeFXVolatilitySwapCalculator CALCULATOR = new CarrLeeFXVolatilitySwapCalculator();

  /**
   * Theta calculator for FX volatility swap based on "bump and reprice" using {@link VolatilitySwapCalculatorResultWithStrikes}, 
   * i.e., assuming the fair value has been already calculated. For theta the bump amount is 1 working day.
   * @param result {@link VolatilitySwapCalculatorResultWithStrikes}
   * @param swap The FX volatility swap 
   * @param data The FX data for Carr-Lee
   * @return theta
   */
  public Double getFXVolatilitySwapTheta(final VolatilitySwapCalculatorResultWithStrikes result, final FXVolatilitySwap swap, final CarrLeeFXData data) {
    ArgumentChecker.notNull(result, "result");
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(data, "data");
    final double spot = data.getSpot();
    final double timeToExpiry = swap.getTimeToMaturity();
    ArgumentChecker.isTrue(Doubles.isFinite(timeToExpiry), "timeToExpiry should be finite");
    ArgumentChecker.isTrue(timeToExpiry > 0., "timeToExpiry should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "spot should be finite");
    ArgumentChecker.isTrue(spot > 0., "spot should be positive");
    final double domesticDF = data.getMulticurveProvider().getDiscountFactor(swap.getBaseCurrency(), timeToExpiry);
    final double foreignDF = data.getMulticurveProvider().getDiscountFactor(swap.getCounterCurrency(), timeToExpiry);
    final double domesticRate = -Math.log(domesticDF) / timeToExpiry;
    final double foreignRate = -Math.log(foreignDF) / timeToExpiry;
    ArgumentChecker.isTrue(Doubles.isFinite(domesticRate), "domestic rate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(foreignRate), "foreign rate should be finite");

    final double baseFV = result.getFairValue();
    final double aFac = swap.getAnnualizationFactor();
    final double timeBumpAmount = 1.0 / aFac;

    final double bumpedTimeToObservationStart = swap.getTimeToObservationStart() == 0. ? 0. : swap.getTimeToObservationStart() - timeBumpAmount;

    final FXVolatilitySwap timeBumpedSwap = new FXVolatilitySwap(bumpedTimeToObservationStart, swap.getTimeToObservationEnd() - timeBumpAmount, swap.getObservationFrequency(),
        swap.getTimeToMaturity() - timeBumpAmount, swap.getVolatilityStrike(), swap.getVolatilityNotional(), swap.getCurrency(), swap.getBaseCurrency(), swap.getCounterCurrency(), aFac);
    final VolatilitySwapCalculatorResult timeBumpedRes = CALCULATOR.visitFXVolatilitySwap(timeBumpedSwap, data);
    final double timeBumpedFV = timeBumpedRes.getFairValue();

    return timeBumpedFV - baseFV;
  }

  @Override
  public Double visitFXVolatilitySwap(final FXVolatilitySwap swap, final CarrLeeFXData data) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(data, "data");

    final CarrLeeFXVolatilitySwapCalculator calculator = new CarrLeeFXVolatilitySwapCalculator();
    final VolatilitySwapCalculatorResultWithStrikes result = calculator.visitFXVolatilitySwap(swap, data);
    return getFXVolatilitySwapTheta(result, swap, data);
  }

}
