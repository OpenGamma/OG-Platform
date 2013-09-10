/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides payoff function and option price function for two-dimensional tree model
 */
public abstract class OptionFunctionProvider2D {
  private double _strike;
  private double _timeToExpiry;
  private int _steps;
  private double _sign;

  /**
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param steps Number of steps
   * @param isCall True if call, false if put
   */
  public OptionFunctionProvider2D(final double strike, final double timeToExpiry, final int steps, final boolean isCall) {
    ArgumentChecker.isTrue(strike >= 0., "strike should be positive");
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
   * @param assetPrice1 Asset price 1 at (nSteps,0), i.e., the price at the lowest node
   * @param assetPrice2 Asset price 2 at (nSteps,0), i.e., the price at the lowest node
   * @param upOverDown1 (up factor)/(down factor) for asset1
   * @param upOverDown2 (up factor)/(down factor) for asset2
   * @return Payoff at expiry 
   */
  public abstract double[][] getPayoffAtExpiry(final double assetPrice1, final double assetPrice2, final double upOverDown1, final double upOverDown2);

  /**
   * @param discount Discount factor
   * @param uuProbability Up-up probability 
   * @param udProbability Up-down probability
   * @param duProbability Down-up probability
   * @param ddProbability Down-down probability
   * @param values Option values in the (steps+1)-th layer
   * @param baseAssetPrice1 Asset price 1 at (0,0), i.e., the starting point
   * @param baseAssetPrice2 Asset price 2 at (0,0), i.e., the starting point
   * @param downFactor1 Down factor for asset1
   * @param downFactor2 Down factor for asset2
   * @param upOverDown1 (up factor)/(down factor) for asset1
   * @param upOverDown2 (up factor)/(down factor) for asset2
   * @param steps  
   * @return Given a set of option values in the (steps+1)-th layer, derive option values in the (steps)-th layer
   */
  public abstract double[][] getNextOptionValues(final double discount, final double uuProbability, final double udProbability, final double duProbability, final double ddProbability,
      final double[][] values, final double baseAssetPrice1, final double baseAssetPrice2, final double downFactor1, final double downFactor2,
      final double upOverDown1, final double upOverDown2, final int steps);

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
    if (!(obj instanceof OptionFunctionProvider2D)) {
      return false;
    }
    OptionFunctionProvider2D other = (OptionFunctionProvider2D) obj;
    if (Double.doubleToLongBits(_sign) != Double.doubleToLongBits(other._sign)) {
      return false;
    }
    if (_steps != other._steps) {
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
