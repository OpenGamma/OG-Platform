/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BermudanOptionFunctionProvider extends OptionFunctionProvider1D {

  private int _nTimes;
  private double[] _exerciseTimes;
  private int[] _exerciseSteps;

  /**
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   * @param exerciseTimes a set of dates on which option can be exercised
   */
  public BermudanOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double[] exerciseTimes) {
    super(strike, timeToExpiry, steps, isCall);

    ArgumentChecker.notNull(exerciseTimes, "exerciseDates");
    _nTimes = exerciseTimes.length;
    for (int i = 0; i < _nTimes; ++i) {
      ArgumentChecker.isTrue(exerciseTimes[i] >= 0., "exerciseDates should be non-negative");
      ArgumentChecker.isTrue(exerciseTimes[i] <= timeToExpiry, "exerciseDates should less than timeToExpiry");
    }

    _exerciseTimes = Arrays.copyOf(exerciseTimes, _nTimes);
    Arrays.sort(_exerciseTimes);

    _exerciseSteps = timesToSteps(steps, timeToExpiry, timeToExpiry / steps);
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown) {
    final double strike = getStrike();
    final int nStepsP = getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice;
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = Math.max(sign * (priceTmp - strike), 0.);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double sumCashDiv,
      final double downFactor, final double upOverDown, final int steps) {
    final double strike = getStrike();
    final double sign = getSign();
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
    int k = _nTimes - 1;
    for (int j = 0; j < nStepsP; ++j) {
      res[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      if (k > -1 && steps == _exerciseTimes[k]) {
        res[j] = Math.max(res[j], sign * (assetPrice + sumCashDiv - strike));
        --k;
      }
      assetPrice *= upOverDown;
    }
    return res;
  }

  /**
   * Access exercise times
   * @return _exerciseTimes
   */
  public double[] getExerciseTimes() {
    return _exerciseTimes;
  }

  /**
   * Access exercise steps
   * @return _exerciseSteps
   */
  public int[] getExerciseSteps() {
    return _exerciseSteps;
  }

  private int[] timesToSteps(final int nSteps, final double timeToExpiry, final double dt) {
    final int[] steps = new int[_nTimes];
    int j = 0;
    for (int i = 0; i < nSteps + 1; ++i) {
      final double currentTime = dt * i;
      if (currentTime >= _exerciseTimes[j]) {
        _exerciseTimes[j] = currentTime;
        ++j;
      }
      if (j == _nTimes) {
        return steps;
      }
    }
    return steps;
  }
}
