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

    ArgumentChecker.isTrue(steps + 1 >= _nTimes, "Number of steps is not large enough");
    for (int i = 0; i < _nTimes; ++i) {
      ArgumentChecker.isTrue(exerciseTimes[i] >= 0., "exerciseDates should be non-negative");
      ArgumentChecker.isTrue(exerciseTimes[i] <= timeToExpiry, "exerciseDates should be less than timeToExpiry");
    }

    _exerciseTimes = Arrays.copyOf(exerciseTimes, _nTimes);
    Arrays.sort(_exerciseTimes);
    _exerciseSteps = timesToSteps(steps, timeToExpiry / steps);
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
      values[i] = Math.max(sign * (priceTmp - strike), 0.);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice, final double sumCashDiv,
      final double downFactor, final double upOverDown, final int steps) {
    final double strike = getStrike();
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    final boolean exercise = checkExercise(steps);
    if (exercise) {
      final double sign = getSign();
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      for (int j = 0; j < nStepsP; ++j) {
        res[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice + sumCashDiv - strike));
        assetPrice *= upOverDown;
      }
    } else {
      for (int j = 0; j < nStepsP; ++j) {
        res[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
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
      values[i] = Math.max(sign * (priceTmp - strike), 0.);
      priceTmp *= middleOverDown;
    }
    return values;
  }

  @Override
  public double[] getNextOptionValues(final double discount, final double upProbability, final double middleProbability, final double downProbability, final double[] values,
      final double baseAssetPrice, final double sumCashDiv, final double downFactor, final double middleOverDown, final int steps) {
    final double strike = getStrike();
    final int nNodes = 2 * steps + 1;

    final double[] res = new double[nNodes];
    final boolean exercise = checkExercise(steps);
    if (exercise) {
      final double sign = getSign();
      double assetPrice = baseAssetPrice * Math.pow(downFactor, steps);
      for (int j = 0; j < nNodes; ++j) {
        res[j] = Math.max(discount * (upProbability * values[j + 2] + middleProbability * values[j + 1] + downProbability * values[j]), sign * (assetPrice + sumCashDiv - strike));
        assetPrice *= middleOverDown;
      }
    } else {
      for (int j = 0; j < nNodes; ++j) {
        res[j] = discount * (upProbability * values[j + 2] + middleProbability * values[j + 1] + downProbability * values[j]);
      }
    }

    return res;
  }

  /**
   * Access number of exercise times
   * @return _nTimes
   */
  public int getNumberOfExerciseTimes() {
    return _nTimes;
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

  private boolean checkExercise(final int currentStep) {
    for (int i = 0; i < _nTimes; ++i) {
      if (currentStep == _exerciseSteps[i]) {
        return true;
      }
    }
    return false;
  }

  private int[] timesToSteps(final int nSteps, final double dt) {
    final int[] steps = new int[_nTimes];
    int j = 0;
    for (int i = 0; i < nSteps + 1; ++i) {
      final double currentTime = dt * i;
      if (currentTime >= _exerciseTimes[j]) {
        final double ref1 = currentTime - _exerciseTimes[j];
        final double ref2 = dt - ref1;
        steps[j] = ref1 <= ref2 ? i : i - 1;
        if (j != 0) {
          ArgumentChecker.isFalse(steps[j] == steps[j - 1], "Number of steps is not large enough");
        }
        ++j;
      }
      if (j == _nTimes) {
        i = nSteps;
      }
    }
    return steps;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_exerciseSteps);
    result = prime * result + Arrays.hashCode(_exerciseTimes);
    result = prime * result + _nTimes;
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
    if (!(obj instanceof BermudanOptionFunctionProvider)) {
      return false;
    }
    BermudanOptionFunctionProvider other = (BermudanOptionFunctionProvider) obj;
    if (!Arrays.equals(_exerciseTimes, other._exerciseTimes)) {
      return false;
    }
    return true;
  }
}
