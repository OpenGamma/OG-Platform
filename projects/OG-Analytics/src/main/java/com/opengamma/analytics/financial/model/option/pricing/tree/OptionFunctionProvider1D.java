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
   * @param assetPrice assetPrice at (nSteps,0), i.e., the price at the lowest node
   * @param upOverDown (up factor)/(down factor)
   * @return Payoff at expiry
   */
  public abstract double[] getPayoffAtExpiry(final double assetPrice, final double upOverDown);

  /**
   * Given a set of option values in the (steps+1)-th layer, derive option values in the (steps)-th layer
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
  public abstract double[] getNextOptionValues(final double discount, final double upProbability, final double downProbability, final double[] values, final double baseAssetPrice,
      final double sumCashDiv, final double downFactor, final double upOverDown, final int steps);

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
}
