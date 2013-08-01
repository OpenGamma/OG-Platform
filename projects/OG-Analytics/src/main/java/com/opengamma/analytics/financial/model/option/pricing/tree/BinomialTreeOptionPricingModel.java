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
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;

    final double[] values = new double[nSteps + 1];
    double assetPrice = spot * Math.pow(downFactor, nSteps);
    final double sig = isCall ? 1. : -1.;
    for (int i = 0; i < nSteps + 1; ++i) {
      values[i] = Math.max(sig * (assetPrice - strike), 0);
      assetPrice *= upOverDown;
    }

    for (int i = nSteps - 1; i > -1; --i) {
      for (int j = 0; j < i + 1; ++j) {
        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
    }

    return values[0];
  }

  public double[] getEuropeanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;

    final double[] values = new double[nSteps + 1];
    final double[] res = new double[4];
    double assetPrice = spot * Math.pow(downFactor, nSteps);
    final double sig = isCall ? 1. : -1.;
    for (int i = 0; i < nSteps + 1; ++i) {
      values[i] = Math.max(sig * (assetPrice - strike), 0);
      assetPrice *= upOverDown;
    }

    double[] pForDelta = new double[] {spot * downFactor, spot * upFactor };
    double[] pForGamma = new double[] {pForDelta[0] * downFactor, pForDelta[0] * upFactor, pForDelta[1] * upFactor };

    for (int i = nSteps - 1; i > -1; --i) {
      for (int j = 0; j < i + 1; ++j) {
        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
      if (i == 2) {
        res[2] = 2. * ((values[2] - values[1]) / (pForGamma[2] - pForGamma[1]) - (values[1] - values[0]) / (pForGamma[1] - pForGamma[0])) / (pForGamma[2] - pForGamma[0]);
        res[3] = values[1];
      }
      if (i == 1) {
        res[1] = (values[1] - values[0]) / (pForDelta[1] - pForDelta[0]);
      }
    }
    res[0] = values[0];
    res[3] = lattice.getTheta(spot, volatility, interestRate, dt, res);
    return res;
  }

  public double getAmericanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    if (isCall) {
      return getEuropeanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, nSteps, true);
    } else {
      final double dt = timeToExpiry / nSteps;
      final double discount = Math.exp(-interestRate * dt);
      final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
      final double upFactor = params[0];
      final double downFactor = params[1];
      final double upProbability = params[2];
      final double downProbability = params[3];
      final double upOverDown = upFactor / downFactor;

      final double[] values = new double[nSteps + 1];
      double assetPrice = spot * Math.pow(downFactor, nSteps);
      for (int i = 0; i < nSteps + 1; ++i) {
        values[i] = Math.max(strike - assetPrice, 0);
        assetPrice *= upOverDown;
      }

      for (int i = nSteps - 1; i > -1; --i) {
        assetPrice = spot * Math.pow(downFactor, i);
        for (int j = 0; j < i + 1; ++j) {
          values[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), strike - assetPrice);
          assetPrice *= upOverDown;
        }
      }

      return values[0];
    }
  }

  public double[] getAmericanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    if (isCall) {
      return getEuropeanGreeks(lattice, spot, strike, timeToExpiry, volatility, interestRate, nSteps, true);
    } else {
      final double[] res = new double[4];

      return res;
    }
  }
}
