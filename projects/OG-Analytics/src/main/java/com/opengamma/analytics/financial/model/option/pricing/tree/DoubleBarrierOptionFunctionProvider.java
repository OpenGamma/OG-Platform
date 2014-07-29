/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * European type of double barrier option
 */
public class DoubleBarrierOptionFunctionProvider extends BarrierOptionFunctionProvider {

  private double _upperBarrier;
  private CrossBarrierChecker _checkerDouble;

  /**
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param lowerBarrier Lower barrier price
   * @param upperBarrier Upper barrier price
   * @param typeName {@link BarrierTypes}, DownAndOut or UpAndOut
   */
  public DoubleBarrierOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double lowerBarrier, final double upperBarrier,
      final BarrierTypes typeName) {
    super(strike, timeToExpiry, steps, isCall, lowerBarrier, BarrierOptionFunctionProvider.BarrierTypes.DownAndOut);
    ArgumentChecker.isTrue(upperBarrier > 0., "upperBarrier should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(upperBarrier), "upperBarrier should be finite");
    ArgumentChecker.isTrue(upperBarrier > lowerBarrier, "upperBarrier should be greater than lowerBarrier");

    switch (typeName) {
      case DoubleKnockOut:
        _checkerDouble = new DoubleBarrier();
        break;
      default:
        throw new NotImplementedException();
    }

    _upperBarrier = upperBarrier;
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nStepsP = nSteps + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = _checkerDouble.checkOut(priceTmp) ? 0. : Math.max(sign * (priceTmp - strike), 0.);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double sumCashDiv,
      final double downFactor, final double upOverDown, final int steps) {
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
    for (int j = 0; j < nStepsP; ++j) {
      res[j] = _checkerDouble.checkOut(assetPrice + sumCashDiv) ? 0. : discount * (upProbability * values[j + 1] + downProbability * values[j]);
      assetPrice *= upOverDown;
    }
    return res;
  }

  @Override
  public double[] getPayoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nNodes = 2 * getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nNodes];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nNodes; ++i) {
      values[i] = _checkerDouble.checkOut(priceTmp) ? 0. : Math.max(sign * (priceTmp - strike), 0.);
      priceTmp *= middleOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double middleProbability, final double downProbability, final double[] values,
      final double baseAssetPrice, final double sumCashDiv, final double downFactor, final double middleOverDown, final int steps) {
    final int nNodes = 2 * steps + 1;

    final double[] res = new double[nNodes];
    double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
    for (int j = 0; j < nNodes; ++j) {
      res[j] = _checkerDouble.checkOut(assetPrice + sumCashDiv) ? 0. : discount * (upProbability * values[j + 2] + middleProbability * values[j + 1] + downProbability * values[j]);
      assetPrice *= middleOverDown;
    }
    return res;
  }

  @Override
  public CrossBarrierChecker getChecker() {
    return this._checkerDouble;
  }

  public BarrierTypes getBarrierType() {
    return BarrierTypes.DoubleKnockOut;
  }

  private CrossBarrierChecker getSuperclassChecker() {
    return super.getChecker();
  }

  /**
   * Access lower barrier
   * @return _barrier in superclass
   */
  public double getLowerBarrier() {
    return super.getBarrier();
  }

  /**
   * Access upper barrier
   * @return _upperBarrier
   */
  public double getUpperBarrier() {
    return _upperBarrier;
  }

  @Override
  public double getBarrier() {
    throw new IllegalArgumentException("Specify lower barrier or upper barrier");
  }

  /**
   * The inherited class checks barriers crossing for double knock-out option
   */
  @SuppressWarnings("synthetic-access")
  protected class DoubleBarrier extends CrossBarrierChecker {

    @Override
    public boolean checkOut(final double priceTmp) {
      return priceTmp >= _upperBarrier || getSuperclassChecker().checkOut(priceTmp);
    }

    @Override
    public boolean checkStrikeBehindBarrier() {
      return getSign() == 1. ? (_upperBarrier <= getStrike()) : false || getSuperclassChecker().checkStrikeBehindBarrier();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_upperBarrier);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof DoubleBarrierOptionFunctionProvider)) {
      return false;
    }
    DoubleBarrierOptionFunctionProvider other = (DoubleBarrierOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_upperBarrier) != Double.doubleToLongBits(other._upperBarrier)) {
      return false;
    }
    return true;
  }
}
