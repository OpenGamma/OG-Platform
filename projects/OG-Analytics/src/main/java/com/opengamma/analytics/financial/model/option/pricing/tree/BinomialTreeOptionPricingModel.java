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
    return getEuropeanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, 0., nSteps, isCall);
  }

  public double getEuropeanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double dividend, final int nSteps, final boolean isCall) {
    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    final double[] values = getPayoffAtExpiry(assetPrice, strike, nSteps, sig, downFactor, upOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      for (int j = 0; j < i + 1; ++j) {
        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
    }

    return values[0];
  }

  public double getEuropeanPricePropotinalDividends(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility,
      final double interestRate, final double[] dividendTimes, final double[] dividends, final int nSteps, final boolean isCall) {

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    final int nDivs = dividendTimes.length;
    double assetPrice = spot * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nDivs; ++i) {
      assetPrice *= (1. - dividends[i]);
    }
    final double[] values = getPayoffAtExpiry(assetPrice, strike, nSteps, sig, downFactor, upOverDown);

    for (int i = nSteps - 1; i > -1; --i) {
      for (int j = 0; j < i + 1; ++j) {
        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
    }

    return values[0];
  }

  public double getEuropeanPriceCashDividends(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility,
      final double interestRate, final double[] dividendTimes, final double[] dividends, final int nSteps, final boolean isCall) {

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    final int nDivs = dividendTimes.length;
    double assetPrice = spot;
    for (int i = 0; i < nDivs; ++i) {
      assetPrice -= dividends[i] * Math.exp(-interestRate * dividendTimes[i]);
    }
    assetPrice *= Math.pow(downFactor, nSteps);
    final double[] values = getPayoffAtExpiry(assetPrice, strike, nSteps, sig, downFactor, upOverDown);
    for (int i = nSteps - 1; i > -1; --i) {
      for (int j = 0; j < i + 1; ++j) {
        values[j] = discount * (upProbability * values[j + 1] + downProbability * values[j]);
      }
    }

    return values[0];
  }

  public double[] getEuropeanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    return getEuropeanGreeks(lattice, spot, strike, timeToExpiry, volatility, interestRate, 0., nSteps, isCall);
  }

  public double[] getEuropeanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double dividend, final int nSteps, final boolean isCall) {
    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    double assetPrice = spot * Math.pow(downFactor, nSteps);
    final double[] values = getPayoffAtExpiry(assetPrice, strike, nSteps, sig, downFactor, upOverDown);
    final double[] res = new double[4];

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
    res[3] = lattice.getTheta(spot, volatility, interestRate, dividend, dt, res);
    return res;
  }

  public double getAmericanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    return getAmericanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, 0., nSteps, isCall);
  }

  public double getAmericanPrice(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double dividend, final int nSteps, final boolean isCall) {
    if (isCall) {
      return getEuropeanPrice(lattice, spot, strike, timeToExpiry, volatility, interestRate, dividend, nSteps, true);
    } else {
      final double dt = timeToExpiry / nSteps;
      final double discount = Math.exp(-interestRate * dt);
      final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
      final double upFactor = params[0];
      final double downFactor = params[1];
      final double upProbability = params[2];
      final double downProbability = params[3];
      final double upOverDown = upFactor / downFactor;
      final double sig = -1.;

      double assetPrice = spot * Math.pow(downFactor, nSteps);
      final double[] values = getPayoffAtExpiry(assetPrice, strike, nSteps, sig, downFactor, upOverDown);

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

  /*
   * TODO error must be returned if dt > (dividend interval)
   */
  public double getAmericanPriceProportionalDividends(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility,
      final double interestRate, final double[] dividendTimes, final double[] dividends, final int nSteps, final boolean isCall) {

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    final int nDivs = dividendTimes.length;
    final int[] divSteps = new int[nDivs];
    for (int i = 0; i < nDivs; ++i) {
      divSteps[i] = (int) (dividendTimes[i] / dt);
    }

    double assetPriceBase = spot;
    for (int i = 0; i < nDivs; ++i) {
      assetPriceBase *= (1. - dividends[i]);
    }
    double assetPrice = assetPriceBase * Math.pow(downFactor, nSteps);
    final double[] values = getPayoffAtExpiry(assetPrice, strike, nSteps, sig, downFactor, upOverDown);

    int counter = 0;
    for (int i = nSteps - 1; i > -1; --i) {
      assetPrice = assetPriceBase * Math.pow(downFactor, i);
      for (int k = nDivs - 1 - counter; k > -1; --k) {
        if (i == divSteps[k]) {
          assetPrice /= (1. - dividends[k]);
          ++counter;
        }
      }
      for (int j = 0; j < i + 1; ++j) {
        values[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sig * (assetPrice - strike));
        assetPrice *= upOverDown;
      }
    }

    return values[0];
  }

  public double getAmericanPriceCashDividends(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility,
      final double interestRate, final double[] dividendTimes, final double[] dividends, final int nSteps, final boolean isCall) {

    final double dt = timeToExpiry / nSteps;
    final double discount = Math.exp(-interestRate * dt);
    final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate, nSteps, dt);
    final double upFactor = params[0];
    final double downFactor = params[1];
    final double upProbability = params[2];
    final double downProbability = params[3];
    final double upOverDown = upFactor / downFactor;
    final double sig = isCall ? 1. : -1.;

    final int nDivs = dividendTimes.length;
    final int[] divSteps = new int[nDivs];
    for (int i = 0; i < nDivs; ++i) {
      divSteps[i] = (int) (dividendTimes[i] / dt);
    }

    double assetPriceBase = spot;
    for (int i = 0; i < nDivs; ++i) {
      assetPriceBase -= dividends[i] * Math.exp(-interestRate * dividendTimes[i]);
    }
    double assetPrice = assetPriceBase * Math.pow(downFactor, nSteps);
    final double[] values = getPayoffAtExpiry(assetPrice, strike, nSteps, sig, downFactor, upOverDown);

    int counter = 0;
    double sumDiscountDiv = 0.;
    for (int i = nSteps - 1; i > -1; --i) {
      assetPrice = assetPriceBase * Math.pow(downFactor, i);
      sumDiscountDiv *= Math.exp(-interestRate * dt);
      for (int k = nDivs - 1 - counter; k > -1; --k) {
        if (i == divSteps[k]) {
          sumDiscountDiv += dividends[k] * Math.exp(-interestRate * (dividendTimes[k] - dt * i));
          ++counter;
        }
      }
      for (int j = 0; j < i + 1; ++j) {
        values[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), sig * (assetPrice + sumDiscountDiv - strike));
        assetPrice *= upOverDown;
      }
    }

    return values[0];
  }

  public double[] getAmericanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final int nSteps, final boolean isCall) {
    return getAmericanGreeks(lattice, spot, strike, timeToExpiry, volatility, interestRate, 0., nSteps, isCall);
  }

  public double[] getAmericanGreeks(final LatticeSpecification lattice, final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate,
      final double dividend, final int nSteps, final boolean isCall) {
    if (isCall) {
      return getEuropeanGreeks(lattice, spot, strike, timeToExpiry, volatility, interestRate, dividend, nSteps, true);
    } else {
      final double[] res = new double[4];

      final double dt = timeToExpiry / nSteps;
      final double discount = Math.exp(-interestRate * dt);
      final double[] params = lattice.getParameters(spot, strike, timeToExpiry, volatility, interestRate - dividend, nSteps, dt);
      final double upFactor = params[0];
      final double downFactor = params[1];
      final double upProbability = params[2];
      final double downProbability = params[3];
      final double upOverDown = upFactor / downFactor;
      final double sig = -1.;

      double assetPrice = spot * Math.pow(downFactor, nSteps);
      final double[] values = getPayoffAtExpiry(assetPrice, strike, nSteps, sig, downFactor, upOverDown);

      double[] pForDelta = new double[] {spot * downFactor, spot * upFactor };
      double[] pForGamma = new double[] {pForDelta[0] * downFactor, pForDelta[0] * upFactor, pForDelta[1] * upFactor };

      for (int i = nSteps - 1; i > -1; --i) {
        assetPrice = spot * Math.pow(downFactor, i);
        for (int j = 0; j < i + 1; ++j) {
          values[j] = Math.max(discount * (upProbability * values[j + 1] + downProbability * values[j]), strike - assetPrice);
          assetPrice *= upOverDown;
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
      res[3] = lattice.getTheta(spot, volatility, interestRate, dividend, dt, res);

      return res;
    }
  }

  // TODO Other type payoff such as Binary type payoff, can be done with OptionDefinition
  private double[] getPayoffAtExpiry(final double assetPrice, final double strike, final int nSteps, final double sig, final double downFactor, final double upOverDown) {
    final double[] values = new double[nSteps + 1];
    //    double assetPrice = spot * Math.pow(downFactor, nSteps);
    double priceTmp = assetPrice;
    for (int i = 0; i < nSteps + 1; ++i) {
      values[i] = Math.max(sig * (priceTmp - strike), 0);
      priceTmp *= upOverDown;
    }
    return values;
  }

}
