/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import java.util.LinkedHashMap;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute delta of forward volatility using finite difference approximation
 */
public class CarrLeeFXVolatilitySwapDeltaCalculator extends InstrumentDerivativeVisitorAdapter<CarrLeeFXData, Double> {

  private static final double DEFAULT_BUMP = 1.0e-5;

  private final CarrLeeFXVolatilitySwapCalculator _cal;
  private final double _bumpSpot;

  /**
   * Constructor using default bump amount
   */
  public CarrLeeFXVolatilitySwapDeltaCalculator() {
    this(DEFAULT_BUMP);
  }

  /**
   * Constructor specifying bump amount
   * @param bump The bump amount
   */
  public CarrLeeFXVolatilitySwapDeltaCalculator(final double bump) {
    _bumpSpot = bump;
    _cal = new CarrLeeFXVolatilitySwapCalculator();
  }

  /**
   * Constructor specifying bump amount and base calculator
   * @param bump The bump amount
   * @param cal Base calculator
   */
  public CarrLeeFXVolatilitySwapDeltaCalculator(final double bump, final CarrLeeFXVolatilitySwapCalculator cal) {
    ArgumentChecker.notNull(cal, "cal");
    _bumpSpot = bump;
    _cal = cal;
  }

  /**
   * Delta calculator for FX volatility swap based on "bump and reprice" using {@link VolatilitySwapCalculatorResultWithStrikes}, 
   * i.e., assuming the fair value has been already calculated. 
   * @param result {@link VolatilitySwapCalculatorResultWithStrikes}
   * @param swap The FX volatility swap 
   * @param data The FX data for Carr-Lee
   * @return Delta
   */
  public Double getFXVolatilitySwapDelta(final VolatilitySwapCalculatorResultWithStrikes result, final FXVolatilitySwap swap, final CarrLeeFXData data) {
    ArgumentChecker.notNull(result, "result");
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(data, "data");

    final double baseFV = result.getFairValue();

    final CarrLeeFXData spotBumpedData = getSpotBumpedData(data);
    final VolatilitySwapCalculatorResult spotBumpedRes = _cal.visitFXVolatilitySwap(swap, spotBumpedData);
    final double spotBumpedFV = spotBumpedRes.getFairValue();

    return (spotBumpedFV - baseFV) / _bumpSpot;
  }

  @Override
  public Double visitFXVolatilitySwap(final FXVolatilitySwap swap, final CarrLeeFXData data) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(data, "data");

    final VolatilitySwapCalculatorResultWithStrikes result = _cal.visitFXVolatilitySwap(swap, data);
    return getFXVolatilitySwapDelta(result, swap, data);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_bumpSpot);
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
    if (!(obj instanceof CarrLeeFXVolatilitySwapDeltaCalculator)) {
      return false;
    }
    CarrLeeFXVolatilitySwapDeltaCalculator other = (CarrLeeFXVolatilitySwapDeltaCalculator) obj;
    if (Double.doubleToLongBits(_bumpSpot) != Double.doubleToLongBits(other._bumpSpot)) {
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
