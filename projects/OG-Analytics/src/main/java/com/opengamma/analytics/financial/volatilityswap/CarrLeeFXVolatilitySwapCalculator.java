/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CarrLeeFXVolatilitySwapCalculator {

  private static final double DEFAULT_LOWEST_PUT_DELTA = -0.1;
  private static final double DEFAULT_HIGHEST_CALL_DELTA = 0.1;
  private static final int DEFAULT_NUM_POINTS = 50;

  private final double _lowestPutDelta;
  private final double _highestCallDelta;
  private final int _numPoints;

  /**
   * Default constructor
   */
  public CarrLeeFXVolatilitySwapCalculator() {
    this(DEFAULT_LOWEST_PUT_DELTA, DEFAULT_HIGHEST_CALL_DELTA, DEFAULT_NUM_POINTS);
  }

  /**
   * Constructor specifying strike range and number of strikes
   * @param lowestPutDelta The delta for put with lowest strike
   * @param highestCallDelta The delta for call with highest strike
   * @param numPoints The number of strikes between the lowest strike and the highest strike is (numPoints + 1)
   */
  public CarrLeeFXVolatilitySwapCalculator(final double lowestPutDelta, final double highestCallDelta, final int numPoints) {
    ArgumentChecker.isTrue(lowestPutDelta < 0. && lowestPutDelta > -1., "-1 < lowestPutDelta < 0 should be true");
    ArgumentChecker.isTrue(highestCallDelta > 0. && highestCallDelta < 1., "0 < highestCallDelta < 1 should be true");
    ArgumentChecker.isTrue(numPoints > 2, "numPoints should be greater than 2");
    _lowestPutDelta = lowestPutDelta;
    _highestCallDelta = highestCallDelta;
    _numPoints = numPoints;
  }

  /**
   * 
   * @param spot The spot
   * @param timeToExpiry The time to expiry
   * @param domesticRate The domestic interest rate
   * @param foreignRate The foreign interest rate
   * @param smile The volatility smile
   * @return {@link VolatilitySwapCalculatorResult}
   */
  final VolatilitySwapCalculatorResult fairValueNew(final double spot, final double timeToExpiry, final double domesticRate, final double foreignRate,
      final SmileDeltaTermStructureParametersStrikeInterpolation smile) {
    ArgumentChecker.notNull(smile, "smile");

    ArgumentChecker.isTrue(Doubles.isFinite(timeToExpiry), "timeToExpiry should be finite");
    ArgumentChecker.isTrue(timeToExpiry > 0., "timeToExpiry should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(spot), "spot should be finite");
    ArgumentChecker.isTrue(spot > 0., "spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(domesticRate), "domesticRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(foreignRate), "foreignRate should be finite");

    final CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator calNew = new CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator();

    final double forward = spot * Math.exp((domesticRate - foreignRate) * timeToExpiry);
    final double[] strikeRange = getStrikeRange(timeToExpiry, smile, forward, 0.);

    final double deltaK = (strikeRange[1] - strikeRange[0]) / _numPoints;
    final double[] strikes = new double[_numPoints + 1];
    for (int i = 0; i < _numPoints; ++i) {
      strikes[i] = strikeRange[0] + deltaK * i;
    }
    strikes[_numPoints] = strikeRange[1];

    final int index = FunctionUtils.getLowerBoundIndex(strikes, forward);
    final int nPuts = index + 1;
    final int nCalls = _numPoints - index;

    final double[] putStrikes = new double[nPuts];
    final double[] callStrikes = new double[nCalls];
    final double[] putVols = new double[nPuts];
    final double[] callVols = new double[nCalls];
    System.arraycopy(strikes, 0, putStrikes, 0, nPuts);
    System.arraycopy(strikes, index + 1, callStrikes, 0, nCalls);
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = smile.getVolatility(timeToExpiry, putStrikes[i], forward);
    }
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = smile.getVolatility(timeToExpiry, callStrikes[i], forward);
    }
    final double strdVol = smile.getVolatility(timeToExpiry, forward, forward);

    return calNew.evaluate(spot, putStrikes, callStrikes, timeToExpiry, domesticRate, foreignRate, putVols, strdVol, callVols);
  }

  /**
   * 
   * @param spot The spot
   * @param timeToExpiry The time to expiry
   * @param timeFromInception The time from inception
   * @param domesticRate The domestic interest rate
   * @param foreignRate The foreign interest rate
   * @param realizedQV The realized variance
   * @param smile The volatility smile
   * @return {@link VolatilitySwapCalculatorResult}
   */
  final VolatilitySwapCalculatorResult fairValueSeasoned(final double spot, final double timeToExpiry, final double timeFromInception, final double domesticRate, final double foreignRate,
      final double realizedQV, final SmileDeltaTermStructureParametersStrikeInterpolation smile) {
    ArgumentChecker.notNull(smile, "smile");

    ArgumentChecker.isTrue(Doubles.isFinite(spot), "spot should be finite");
    ArgumentChecker.isTrue(spot > 0., "spot should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(timeToExpiry), "timeToExpiry should be finite");
    ArgumentChecker.isTrue(timeToExpiry > 0., "timeToExpiry should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(timeFromInception), "timeFromInception should be finite");
    ArgumentChecker.isTrue(timeFromInception > 0., "timeFromInception should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(domesticRate), "domesticRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(foreignRate), "foreignRate should be finite");
    ArgumentChecker.isTrue(Doubles.isFinite(realizedQV), "realizedQV should be finite");
    ArgumentChecker.isTrue(realizedQV > 0., "realizedQV should be positive");

    final CarrLeeSeasonedSyntheticVolatilitySwapCalculator calSeasoned = new CarrLeeSeasonedSyntheticVolatilitySwapCalculator();

    final double forward = spot * Math.exp((domesticRate - foreignRate) * timeToExpiry);
    final double reference = 3.0 * Math.sqrt(realizedQV * timeFromInception) / 100.;
    final double[] strikeRange = getStrikeRange(timeToExpiry, smile, forward, reference);

    final double deltaK = (strikeRange[1] - strikeRange[0]) / _numPoints;
    final double[] strikes = new double[_numPoints + 1];
    for (int i = 0; i < _numPoints; ++i) {
      strikes[i] = strikeRange[0] + deltaK * i;
    }
    strikes[_numPoints] = strikeRange[1];

    final int index = FunctionUtils.getLowerBoundIndex(strikes, forward);
    final int nPuts = index + 1;
    final int nCalls = _numPoints - index;

    final double[] putStrikes = new double[nPuts];
    final double[] callStrikes = new double[nCalls];
    final double[] putVols = new double[nPuts];
    final double[] callVols = new double[nCalls];
    System.arraycopy(strikes, 0, putStrikes, 0, nPuts);
    System.arraycopy(strikes, index + 1, callStrikes, 0, nCalls);
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = smile.getVolatility(timeToExpiry, putStrikes[i], forward);
    }
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = smile.getVolatility(timeToExpiry, callStrikes[i], forward);
    }

    return calSeasoned.evaluate(spot, putStrikes, callStrikes, timeToExpiry, timeFromInception, domesticRate, foreignRate, putVols, callVols, realizedQV);
  }

  private double[] getStrikeRange(final double timeToExpiry, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final double forward, final double reference) {
    final double[] res = new double[2];
    res[0] = findStrike(_lowestPutDelta, timeToExpiry, smile, forward, false);
    res[1] = findStrike(_highestCallDelta, timeToExpiry, smile, forward, true);
    if (reference == 0.) {
      return res;
    }
    res[0] = Math.min(res[0], forward * Math.exp(-reference));
    res[1] = Math.max(res[1], forward * Math.exp(reference));
    return res;
  }

  private double findStrike(final double delta, final double timeToExpiry, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final double forward, final boolean isCall) {
    final Function1D<Double, Double> func = getDeltaDifference(delta, timeToExpiry, smile, forward, isCall);
    final BisectionSingleRootFinder rtFinder = new BisectionSingleRootFinder(1.e-12);
    final double strike = rtFinder.getRoot(func, forward * 0.2, forward * 1.8);
    return strike;
  }

  private Function1D<Double, Double> getDeltaDifference(final double delta, final double timeToExpiry, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final double forward,
      final boolean isCall) {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        final double vol = smile.getVolatility(timeToExpiry, strike, forward);
        return BlackFormulaRepository.delta(forward, strike, timeToExpiry, vol, isCall) - delta;
      }
    };
  }

}
