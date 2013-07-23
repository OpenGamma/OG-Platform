/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

/**
 * 
 */
public class BinomialTreeOptionPricingModel extends TreeOptionPricingModel {

  public double getEuropeanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, dt, volatility, interestRate);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;

    final double[] payoff = new double[nSteps + 1];
    double assetPrice = spot * Math.pow(downFactor, nSteps);
    if (isCall) {
      for (int i = 0; i < nSteps + 1; ++i) {
        //      final double assetPrice = spot * Math.pow(downFactor, nSteps - i) * Math.pow(upFactor, i);
        payoff[i] = Math.max(assetPrice - strike, 0);
        assetPrice *= upOverDown;
      }
    } else {
      for (int i = 0; i < nSteps + 1; ++i) {
        //      final double assetPrice = spot * Math.pow(downFactor, nSteps - i) * Math.pow(upFactor, i);
        payoff[i] = Math.max(strike - assetPrice, 0);
        assetPrice *= upOverDown;
      }
    }

    for (int i = nSteps - 1; i > -1; --i) {
      for (int j = 0; j < i + 1; ++j) {
        payoff[j] = discount * (upProbability * payoff[j + 1] + downProbability * payoff[j]);
      }
    }

    return payoff[0];
  }

}
