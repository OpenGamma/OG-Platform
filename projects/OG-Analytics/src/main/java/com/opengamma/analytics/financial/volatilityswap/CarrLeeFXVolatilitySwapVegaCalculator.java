/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Compute vega of forward volatility by parallel shift to volatility surface
 */
public class CarrLeeFXVolatilitySwapVegaCalculator extends InstrumentDerivativeVisitorAdapter<CarrLeeFXData, Double> {

  private static final double DEFAULT_BUMP = 1.0e-7;
  private static final CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator NEW_CALCULATOR = new CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator();
  private static final CarrLeeSeasonedSyntheticVolatilitySwapCalculator SEASONED_CALCULATOR = new CarrLeeSeasonedSyntheticVolatilitySwapCalculator();

  private final CarrLeeFXVolatilitySwapCalculator _cal;
  private final double _bumpVol;

  /**
   * Constructor using default bump amount. 
   * Note that fractional volatility is bumped
   */
  public CarrLeeFXVolatilitySwapVegaCalculator() {
    this(DEFAULT_BUMP);
  }

  /**
   * Constructor specifying bump amount
   * @param bump The bump amount
   */
  public CarrLeeFXVolatilitySwapVegaCalculator(final double bump) {
    _bumpVol = bump;
    _cal = new CarrLeeFXVolatilitySwapCalculator();
  }

  /**
   * Constructor specifying bump amount
   * @param bump The bump amount
   * @param cal Base calculator
   */
  public CarrLeeFXVolatilitySwapVegaCalculator(final double bump, final CarrLeeFXVolatilitySwapCalculator cal) {
    ArgumentChecker.notNull(cal, "cal");
    _bumpVol = bump;
    _cal = cal;
  }

  /**
   * Vega calculator for FX volatility swap based on "bump and reprice" using {@link VolatilitySwapCalculatorResultWithStrikes}, 
   * i.e., assuming the fair value has been already calculated. 
   * @param result {@link VolatilitySwapCalculatorResultWithStrikes}
   * @param swap The FX volatility swap 
   * @param data The FX data for Carr-Lee
   * @return vega
   */
  public Double getFXVolatilitySwapVega(final VolatilitySwapCalculatorResultWithStrikes result, final FXVolatilitySwap swap, final CarrLeeFXData data) {
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
    final double[] bumpedPutVols = new double[nPuts];
    final double[] bumpedCallVols = new double[nCalls];
    for (int i = 0; i < nPuts; ++i) {
      putVols[i] = smile.getVolatility(Triple.of(timeToExpiry, putStrikes[i], forward));
      bumpedPutVols[i] = putVols[i] + _bumpVol;
    }
    for (int i = 0; i < nCalls; ++i) {
      callVols[i] = smile.getVolatility(Triple.of(timeToExpiry, callStrikes[i], forward));
      bumpedCallVols[i] = callVols[i] + _bumpVol;
    }

    final double baseFV = result.getFairValue();
    final double volBumpedFV;

    final Double rv = data.getRealizedVariance();

    if (rv == null) {
      final double stdVol = smile.getVolatility(Triple.of(timeToExpiry, forward, forward));
      final double bumpedStdVol = stdVol + _bumpVol;
      final VolatilitySwapCalculatorResult volBumpedRes = NEW_CALCULATOR.evaluate(spot, putStrikes, callStrikes, timeToExpiry, domesticRate, foreignRate, bumpedPutVols, bumpedStdVol, bumpedCallVols);
      volBumpedFV = volBumpedRes.getFairValue();
    } else {
      final double timeFromInception = swap.getTimeToObservationStart() < 0 ? Math.abs(swap.getTimeToObservationStart()) : 0;
      final VolatilitySwapCalculatorResult volBumpedRes = SEASONED_CALCULATOR.evaluate(spot, putStrikes, callStrikes, timeToExpiry, timeFromInception, domesticRate, foreignRate, bumpedPutVols,
          bumpedCallVols, rv);
      volBumpedFV = volBumpedRes.getFairValue();
    }

    return (volBumpedFV - baseFV) / _bumpVol * 1.0e-2;
  }

  @Override
  public Double visitFXVolatilitySwap(final FXVolatilitySwap swap, final CarrLeeFXData data) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(data, "data");

    final VolatilitySwapCalculatorResultWithStrikes result = _cal.visitFXVolatilitySwap(swap, data);
    return getFXVolatilitySwapVega(result, swap, data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_bumpVol);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_cal == null) ? 0 : _cal.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CarrLeeFXVolatilitySwapVegaCalculator)) {
      return false;
    }
    CarrLeeFXVolatilitySwapVegaCalculator other = (CarrLeeFXVolatilitySwapVegaCalculator) obj;
    if (Double.doubleToLongBits(_bumpVol) != Double.doubleToLongBits(other._bumpVol)) {
      return false;
    }
    if (_cal == null) {
      if (other._cal != null) {
        return false;
      }
    } else if (!_cal.equals(other._cal)) {
      return false;
    }
    return true;
  }

}
