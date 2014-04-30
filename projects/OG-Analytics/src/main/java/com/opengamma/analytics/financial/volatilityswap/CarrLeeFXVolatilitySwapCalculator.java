/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class CarrLeeFXVolatilitySwapCalculator extends InstrumentDerivativeVisitorAdapter<CarrLeeFXData, VolatilitySwapCalculatorResult> {
  private static final double DEFAULT_LOWEST_PUT_DELTA = -0.1;
  private static final double DEFAULT_HIGHEST_CALL_DELTA = 0.1;
  private static final int DEFAULT_NUM_POINTS = 50;
  private static final CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator NEW_CALCULATOR = new CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator();
  private static final CarrLeeSeasonedSyntheticVolatilitySwapCalculator SEASONED_CALCULATOR = new CarrLeeSeasonedSyntheticVolatilitySwapCalculator();
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

  @Override
  public VolatilitySwapCalculatorResultWithStrikes visitFXVolatilitySwap(final FXVolatilitySwap swap, final CarrLeeFXData data) {
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
    final double forward = spot * foreignDF / domesticDF;
    final double timeFromInception = swap.getTimeToObservationStart() < 0 ? Math.abs(swap.getTimeToObservationStart()) : 0;
    final double[] strikeRange;
    if (swap.getTimeToObservationStart() < 0) {
      if (data.getRealizedVariance() == null) {
        throw new IllegalStateException("Trying to price a seasoned swap but have null realized variance in the market data object");
      }
      final double reference = 3.0 * Math.sqrt(data.getRealizedVariance() * timeFromInception) / 100.;
      strikeRange = getStrikeRange(timeToExpiry, data.getVolatilityData(), forward, reference);
    } else {
      strikeRange = getStrikeRange(timeToExpiry, data.getVolatilityData(), forward, 0.);
    }
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
      putVols[i] = data.getVolatilityData().getVolatility(Triple.of(timeToExpiry, putStrikes[i], forward));
    }
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = data.getVolatilityData().getVolatility(Triple.of(timeToExpiry, callStrikes[i], forward));
    }
    if (swap.getTimeToObservationStart() < 0) {
      return (SEASONED_CALCULATOR.evaluate(spot, putStrikes, callStrikes, timeToExpiry, timeFromInception, domesticRate,
          foreignRate, putVols, callVols, data.getRealizedVariance()).withStrikes(putStrikes, callStrikes));
    }
    final double strdVol = data.getVolatilityData().getVolatility(Triple.of(timeToExpiry, forward, forward));
    return (NEW_CALCULATOR.evaluate(spot, putStrikes, callStrikes, timeToExpiry, domesticRate, foreignRate, putVols, strdVol, callVols)).withStrikes(putStrikes, callStrikes);
  }

  private double[] getStrikeRange(final double timeToExpiry, final SmileDeltaTermStructureParameters smile, final double forward, final double reference) {
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

  private double findStrike(final double delta, final double timeToExpiry, final SmileDeltaTermStructureParameters smile, final double forward, final boolean isCall) {
    final Function1D<Double, Double> func = getDeltaDifference(delta, timeToExpiry, smile, forward, isCall);
    final BisectionSingleRootFinder rtFinder = new BisectionSingleRootFinder(1.e-12);
    final double strike = rtFinder.getRoot(func, forward * 0.2, forward * 1.8);
    return strike;
  }

  private Function1D<Double, Double> getDeltaDifference(final double delta, final double timeToExpiry, final SmileDeltaTermStructureParameters smile, final double forward,
      final boolean isCall) {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        final double vol = smile.getVolatility(Triple.of(timeToExpiry, strike, forward));
        return BlackFormulaRepository.delta(forward, strike, timeToExpiry, vol, isCall) - delta;
      }
    };
  }

}
