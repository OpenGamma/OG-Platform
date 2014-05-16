/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import java.util.LinkedHashMap;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Compute delta, vega and theta by using finite difference method
 */
public class VolatilitySwapFiniteDifferenceGreeksCalculator {

  private static final double DEFAULT_BUMP = 1.0e-5;
  private static final CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator NEW_CALCULATOR = new CarrLeeNewlyIssuedSyntheticVolatilitySwapCalculator();
  private static final CarrLeeSeasonedSyntheticVolatilitySwapCalculator SEASONED_CALCULATOR = new CarrLeeSeasonedSyntheticVolatilitySwapCalculator();
  private static final CarrLeeFXVolatilitySwapCalculator COMBINED_CALCULATOR = new CarrLeeFXVolatilitySwapCalculator();

  private final double _bumpSpot;
  private final double _bumpVol;

  /**
   * Constructor using default bump amount
   */
  public VolatilitySwapFiniteDifferenceGreeksCalculator() {
    _bumpSpot = DEFAULT_BUMP;
    _bumpVol = _bumpSpot * 1.0e-2;
  }

  /**
   * Constructor specifying bump amount
   * @param bump The bump amount
   */
  public VolatilitySwapFiniteDifferenceGreeksCalculator(final double bump) {
    _bumpSpot = bump;
    _bumpVol = _bumpSpot * 1.0e-2;
  }

  /**
   * Greeks calculator for FX volatility swap based on "bump and reprice" using {@link VolatilitySwapCalculatorResultWithStrikes}, 
   * i.e., assuming the fair value has been already calculated. For theta the bump amount is 1 working day.
   * @param result {@link VolatilitySwapCalculatorResultWithStrikes}
   * @param swap The FX volatility swap 
   * @param data The FX data for Carr-Lee
   * @return Array of {delta, vega, theta}
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

    final double[] res = new double[3];
    final double baseFV = result.getFairValue();
    final double volBumpedFV;

    final Double rv = data.getRealizedVariance();
    final double aFac = swap.getAnnualizationFactor();
    final double timeBumpAmount = 1.0 / aFac;

    final double bumpedTimeToObservationStart = swap.getTimeToObservationStart() == 0. ? 0. : swap.getTimeToObservationStart() - timeBumpAmount;

    final FXVolatilitySwap timeBumpedSwap = new FXVolatilitySwap(bumpedTimeToObservationStart, swap.getTimeToObservationEnd() - timeBumpAmount, swap.getObservationFrequency(),
        swap.getTimeToMaturity() - timeBumpAmount, swap.getVolatilityStrike(), swap.getVolatilityNotional(), swap.getCurrency(), swap.getBaseCurrency(), swap.getCounterCurrency(), aFac);
    final VolatilitySwapCalculatorResult timeBumpedRes = COMBINED_CALCULATOR.visitFXVolatilitySwap(timeBumpedSwap, data);
    final double timeBumpedFV = timeBumpedRes.getFairValue();

    final CarrLeeFXData spotBumpedData = getSpotBumpedData(data);
    final VolatilitySwapCalculatorResult spotBumpedRes = COMBINED_CALCULATOR.visitFXVolatilitySwap(swap, spotBumpedData);
    final double spotBumpedFV = spotBumpedRes.getFairValue();

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
    res[0] = (spotBumpedFV - baseFV) / _bumpSpot;
    res[1] = (volBumpedFV - baseFV) / _bumpVol * 1.0e-2;
    res[2] = timeBumpedFV - baseFV;

    return res;
  }

  /**
   * Greeks calculator for FX volatility swap based on "bump and reprice." 
   * For theta the bump amount is 1 working day.
   * @param swap The FX volatility swap 
   * @param data The FX data for Carr-Lee
   * @return Array of {delta, vega, theta}
   */
  public double[] getFXVolatilitySwapGreeks(final FXVolatilitySwap swap, final CarrLeeFXData data) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(data, "data");

    final CarrLeeFXVolatilitySwapCalculator calculator = new CarrLeeFXVolatilitySwapCalculator();
    final VolatilitySwapCalculatorResultWithStrikes result = calculator.visitFXVolatilitySwap(swap, data);
    return getFXVolatilitySwapGreeks(result, swap, data);
  }

  private CarrLeeFXData getSpotBumpedData(final CarrLeeFXData data) {
    final FXMatrix spotBumpedfxMatrix = new FXMatrix(data.getCurrencyPair().getFirst(), data.getCurrencyPair().getSecond(), data.getSpot() + _bumpSpot);

    final MulticurveProviderInterface provider = data.getMulticurveProvider();
    final MulticurveProviderDiscount spotBumpedCurves;

    if (provider instanceof MulticurveProviderDiscount) {
      final MulticurveProviderDiscount discountCurves = (MulticurveProviderDiscount) data.getMulticurveProvider();
      spotBumpedCurves = new MulticurveProviderDiscount(discountCurves.getDiscountingCurves(), new LinkedHashMap<IborIndex, YieldAndDiscountCurve>(),
          new LinkedHashMap<IndexON, YieldAndDiscountCurve>(), spotBumpedfxMatrix);
    } else {
      throw new IllegalArgumentException("Multi-curve provider should be an instance of MulticurveProviderDiscount");
    }

    if (data.getRealizedVariance() == null) {
      return new CarrLeeFXData(data.getCurrencyPair(), data.getVolatilityData(), spotBumpedCurves);
    }
    return new CarrLeeFXData(data.getCurrencyPair(), data.getVolatilityData(), spotBumpedCurves, data.getRealizedVariance());
  }

}
