/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides payoff function and option price function for one-dimensional tree model
 */
public abstract class OptionFunctionProvider1D {
  private double _strike;
  private double _timeToExpiry;
  private int _steps;
  private double _sign;

  /**
   * Superclass constructor 
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   */
  public OptionFunctionProvider1D(final double strike, final double timeToExpiry, final int steps, final boolean isCall) {
    ArgumentChecker.isTrue(strike > 0., "strike should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(strike), "strike should be finite");
    ArgumentChecker.isTrue(timeToExpiry > 0., "timeToExpiry should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(timeToExpiry), "timeToExpiry should be finite");
    ArgumentChecker.isTrue(steps > 2, "The number of steps should be greater than 2");

    _strike = strike;
    _timeToExpiry = timeToExpiry;
    _steps = steps;
    _sign = isCall ? 1. : -1.;
  }

  /**
   * @param assetPrice The base asset price: spot or modified spot
   * @param downFactor Down factor
   * @param upOverDown (up factor)/(down factor)
   * @return Payoff at expiry
   */
  public abstract double[] getPayoffAtExpiry(final double assetPrice, final double downFactor, final double upOverDown);

  /**
   * Given a set of option values in the (steps+1)-th layer, derive option values in the (steps)-th layer
   * For an option with early exercise feature, this method should be overridden
   * @param discount Discount factor
   * @param upProbability Up probability
   * @param downProbability Down probability
   * @param values Option values in the (steps+1)-th layer
   * @param baseAssetPrice Asset price at (0,0), i.e., the starting point
   * @param sumCashDiv Sum of discounted discrete cash dividends payed after (steps+1)-th layer
   * @param downFactor Down factor 
   * @param upOverDown  (up factor)/(down factor)
   * @param steps  
   * @return Option values in the (steps)-th layer
   */
  public double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice,
      final double sumCashDiv, final double downFactor, final double upOverDown, final int steps) {
    final int nStepsP = steps + 1;

    final double[] res = new double[nStepsP];
    for (int j = 0; j < nStepsP; ++j) {
      res[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
    }
    return res;
  }

  /**
   * @param assetPrice (Modified) assetPrice at (0,0)
   * @param downFactor Down factor
   * @param middleOverDown (middle factor)/(down factor)
   * @return Payoff at expiry
   */
  public abstract double[] getPayoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown);

  /**
   * Given a set of option values in the (steps+1)-th layer, derive option values in the (steps)-th layer
   * For an option with early exercise feature or barriers, this method should be overridden
   * @param discount Discount factor
   * @param upProbability Up probability
   * @param middleProbability Middle probability
   * @param downProbability Down probability
   * @param values Option values in the (steps+1)-th layer
   * @param baseAssetPrice Asset price at (0,0), i.e., the starting point
   * @param sumCashDiv Sum of discounted discrete cash dividends payed after (steps+1)-th layer
   * @param downFactor Down factor 
   * @param middleOverDown  (middle factor)/(down factor)
   * @param steps  
   * @return Option values in the (steps)-th layer
   */
  public double[] getNextOptionValues(final double discount, final double upProbability, final double middleProbability, final double downProbability, final double[] values,
      final double baseAssetPrice, final double sumCashDiv, final double downFactor, final double middleOverDown, final int steps) {
    final int nNodes = 2 * steps + 1;

    final double[] res = new double[nNodes];
    for (int j = 0; j < nNodes; ++j) {
      res[j] = discount * (upProbability * values[j + 2] + middleProbability * values[j + 1] + downProbability * values[j]);
    }
    return res;
  }

  /**
   * Access strike price
   * @return _strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Access time to expiry
   * @return _timeToExpiry
   */
  public double getTimeToExpiry() {
    return _timeToExpiry;
  }

  /**
   * Access number of steps
   * @return _steps
   */
  public int getNumberOfSteps() {
    return _steps;
  }

  /**
   * Access signature in payoff formula
   * @return +1 if call, -1 if put
   */
  public double getSign() {
    return _sign;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_sign);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _steps;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_timeToExpiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof OptionFunctionProvider1D)) {
      return false;
    }
    OptionFunctionProvider1D other = (OptionFunctionProvider1D) obj;
    if (Double.doubleToLongBits(_sign) != Double.doubleToLongBits(other._sign)) {
      return false;
    }
    if (_steps != other.getNumberOfSteps()) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToExpiry) != Double.doubleToLongBits(other._timeToExpiry)) {
      return false;
    }
    return true;
  }
}
