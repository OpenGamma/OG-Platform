/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class VolatilitySwapGreeksCalculator {

  /**
   * Greeks calculator for FX volatility swap based on result from Carr-Lee model
   * @param result {@link VolatilitySwapCalculatorResultWithStrikes}
   * @param swap The FX volatility swap 
   * @param data The FX data for Carr-Lee
   * @return Array of {delta, gamma, vega, theta}
   */
  public double[] getFXVolatilitySwapGreeks(final VolatilitySwapCalculatorResultWithStrikes result, final FXVolatilitySwap swap, final CarrLeeFXData data) {
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

    final double[] putStrikes = result.getPutStrikes();
    final double forward = spot * Math.exp((domesticRate - foreignRate) * timeToExpiry);
    final double[] callStrikes = result.getCallStrikes();

    final int nPuts = putStrikes.length;
    final int nCalls = callStrikes.length;
    final SmileDeltaTermStructureParameters smile = data.getVolatilityData();
    final double[] putVols = new double[nPuts];
    final double[] callVols = new double[nCalls];

    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = smile.getVolatility(Triple.of(timeToExpiry, putStrikes[i], forward));
    }
    final double stdVol = smile.getVolatility(Triple.of(timeToExpiry, forward, forward));
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = smile.getVolatility(Triple.of(timeToExpiry, callStrikes[i], forward));
    }

    return getGreeks(result, spot, putStrikes, callStrikes, timeToExpiry, domesticRate, foreignRate, putVols, stdVol, callVols);
  }

  /**
   * Greeks calculator for FX volatility swap using Carr-Lee model
   * @param swap The FX volatility swap 
   * @param data The FX data for Carr-Lee
   * @return Array of {delta, gamma, vega, theta}
   */
  public double[] getFXVolatilitySwapGreeks(final FXVolatilitySwap swap, final CarrLeeFXData data) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(data, "data");

    final CarrLeeFXVolatilitySwapCalculator calculator = new CarrLeeFXVolatilitySwapCalculator();
    final VolatilitySwapCalculatorResultWithStrikes result = calculator.visitFXVolatilitySwap(swap, data);
    return getFXVolatilitySwapGreeks(result, swap, data);
  }

  private double[] getGreeks(final VolatilitySwapCalculatorResult result, final double spot, final double[] putStrikes, final double[] callStrikes, final double timeToExpiry,
      final double interestRate, final double dividend, final double[] putVols, final double stdVol, final double[] callVols) {

    final int nCalls = callStrikes.length;
    final int nPuts = putStrikes.length;
    final double rate = interestRate - dividend;
    final double forward = spot * Math.exp(rate * timeToExpiry);

    final double[] putWeights = result.getPutWeights();
    final double straddleWeight = result.getStraddleWeight();
    final double[] callWeights = result.getCallWeights();

    final int num = 4;
    final double[] res = new double[num];

    for (int i = 0; i < nPuts; ++i) {
      getGreeksFraction(res, putWeights[i], spot, putStrikes[i], timeToExpiry, putVols[i], interestRate, rate, false);
    }
    if (straddleWeight != 0.) {
      getGreeksFraction(res, straddleWeight, spot, forward, timeToExpiry, stdVol, interestRate, rate, true);
    }
    for (int i = 0; i < nCalls; ++i) {
      getGreeksFraction(res, callWeights[i], spot, callStrikes[i], timeToExpiry, callVols[i], interestRate, rate, true);
    }

    return res;
  }

  private void getGreeksFraction(final double[] res, final double weight, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double costOfCarry, final boolean isCall) {
    res[0] += weight * BlackScholesFormulaRepository.delta(spot, strike, timeToExpiry, volatility, interestRate, costOfCarry, isCall);
    res[1] += weight * BlackScholesFormulaRepository.gamma(spot, strike, timeToExpiry, volatility, interestRate, costOfCarry);
    res[2] += weight * BlackScholesFormulaRepository.vega(spot, strike, timeToExpiry, volatility, interestRate, costOfCarry);
    res[3] -= weight * BlackScholesFormulaRepository.theta(spot, strike, timeToExpiry, volatility, interestRate, costOfCarry, isCall);
  }
}
