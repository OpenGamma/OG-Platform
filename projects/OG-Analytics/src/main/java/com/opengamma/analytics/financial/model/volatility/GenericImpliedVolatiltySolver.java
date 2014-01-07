/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;

/**
 * Finds an implied volatility (a parameter that put into a model gives the market pirce of an option) for any option pricing model that has a 'volatility' parameter.
 *  This included the Black-Scholes-Merton model (and derivatives) for European options and Barone-Adesi & Whaley and Bjeksund and Stensland for American options   
 */
public class GenericImpliedVolatiltySolver {

  private static final int MAX_ITERATIONS = 20; //something's wrong if Newton-Raphson taking longer than this
  private static final double VOL_TOL = 1e-9; // 1 part in 100,000 basis points will do for implied vol

  public static double impliedVolatility(final double optionPrice, final Function1D<Double, double[]> pavFunc) {

    double lowerSigma;
    double upperSigma;
    final double volGuess = 0.3;

    try {
      final double[] temp = bracketRoot(optionPrice, pavFunc, volGuess, Math.min(volGuess, 0.1));
      lowerSigma = temp[0];
      upperSigma = temp[1];
    } catch (final MathException e) {
      throw new IllegalArgumentException(e.toString() + " No implied Volatility for this price. [price: " + optionPrice + "]");
    }
    double sigma = (lowerSigma + upperSigma) / 2.0;
    final double maxChange = 0.5;

    double[] pnv = pavFunc.evaluate(sigma);

    //This can happen for American options, where low volatilities puts you in the early excise region which obviously has zero vega 
    if (pnv[1] == 0 || Double.isNaN(pnv[1])) {
      return solveByBisection(optionPrice, pavFunc, lowerSigma, upperSigma);
    }
    double diff = pnv[0] / optionPrice - 1.0;
    boolean above = diff > 0;
    if (above) {
      upperSigma = sigma;
    } else {
      lowerSigma = sigma;
    }

    double trialChange = -diff * optionPrice / pnv[1];
    double actChange;
    if (trialChange > 0.0) {
      actChange = Math.min(maxChange, Math.min(trialChange, upperSigma - sigma));
    } else {
      actChange = Math.max(-maxChange, Math.max(trialChange, lowerSigma - sigma));
    }

    int count = 0;
    while (Math.abs(actChange) > VOL_TOL) {
      sigma += actChange;
      pnv = pavFunc.evaluate(sigma);

      if (pnv[1] == 0 || Double.isNaN(pnv[1])) {
        return solveByBisection(optionPrice, pavFunc, lowerSigma, upperSigma);
      }

      diff = pnv[0] / optionPrice - 1.0;
      above = diff > 0;
      if (above) {
        upperSigma = sigma;
      } else {
        lowerSigma = sigma;
      }

      trialChange = -diff * optionPrice / pnv[1];
      if (trialChange > 0.0) {
        actChange = Math.min(maxChange, Math.min(trialChange, upperSigma - sigma));
      } else {
        actChange = Math.max(-maxChange, Math.max(trialChange, lowerSigma - sigma));
      }

      if (count++ > MAX_ITERATIONS) {
        return solveByBisection(optionPrice, pavFunc, lowerSigma, upperSigma);
      }
    }
    return sigma;

  }

  /**
   * Compute implied volatility with geuss value as an input
   * @param optionPrice Option price
   * @param pavFunc Model
   * @param guess Guess value
   * @return Implied volatility 
   */
  public static double impliedVolatility(final double optionPrice, final Function1D<Double, double[]> pavFunc, final double guess) {

    double lowerSigma;
    double upperSigma;
    final double volGuess = guess < 1.e-2 ? 0.15 : guess;
    final double shift = 1.e-2;

    try {
      final double[] temp = bracketRoot(optionPrice, pavFunc, volGuess, Math.min(volGuess, 0.1), shift);
      lowerSigma = temp[0];
      upperSigma = temp[1];
    } catch (final MathException e) {
      throw new IllegalArgumentException(e.toString() + " No implied Volatility for this price. [price: " + optionPrice + "]");
    }
    double sigma = (lowerSigma + upperSigma) / 2.0;
    final double maxChange = 0.5;

    double[] pnv = pavFunc.evaluate(sigma);

    //This can happen for American options, where low volatilities puts you in the early excise region which obviously has zero vega 
    if (Math.abs(pnv[1]) < 1.e-14 || Double.isNaN(pnv[1])) {
      return solveByBisection(optionPrice, pavFunc, lowerSigma, upperSigma);
    }
    double diff = pnv[0] / optionPrice - 1.0;
    boolean above = diff > 0;
    if (above) {
      upperSigma = sigma;
    } else {
      lowerSigma = sigma;
    }

    double trialChange = -diff * optionPrice / pnv[1];
    double actChange;
    if (trialChange > 0.0) {
      actChange = Math.min(maxChange, Math.min(trialChange, upperSigma - sigma));
    } else {
      actChange = Math.max(-maxChange, Math.max(trialChange, lowerSigma - sigma));
    }

    int count = 0;
    while (Math.abs(actChange) > VOL_TOL) {
      sigma += actChange;
      pnv = pavFunc.evaluate(sigma);

      if (Math.abs(pnv[1]) < 1.e-14 || Double.isNaN(pnv[1])) {
        return solveByBisection(optionPrice, pavFunc, lowerSigma, upperSigma);
      }

      diff = pnv[0] / optionPrice - 1.0;
      above = diff > 0;
      if (above) {
        upperSigma = sigma;
      } else {
        lowerSigma = sigma;
      }

      trialChange = -diff * optionPrice / pnv[1];
      if (trialChange > 0.0) {
        actChange = Math.min(maxChange, Math.min(trialChange, upperSigma - sigma));
      } else {
        actChange = Math.max(-maxChange, Math.max(trialChange, lowerSigma - sigma));
      }

      if (count++ > MAX_ITERATIONS) {
        return solveByBisection(optionPrice, pavFunc, lowerSigma, upperSigma);
      }
    }
    return sigma;

  }

  private static double[] bracketRoot(final double optionPrice, final Function1D<Double, double[]> pavFunc, final double sigma, final double change) {
    final BracketRoot bracketer = new BracketRoot();
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double volatility) {
        return pavFunc.evaluate(volatility)[0] / optionPrice - 1.0;
      }
    };
    return bracketer.getBracketedPoints(func, sigma - Math.abs(change), sigma + Math.abs(change), 0, Double.POSITIVE_INFINITY);
  }

  private static double[] bracketRoot(final double optionPrice, final Function1D<Double, double[]> pavFunc, final double sigma, final double change, final double shift) {
    final BracketRoot bracketer = new BracketRoot();
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double volatility) {
        return pavFunc.evaluate(volatility)[0] / optionPrice - 1.0;
      }
    };
    final double absChange = Math.abs(change);
    final double left = sigma - absChange < shift ? shift : sigma - absChange;
    return bracketer.getBracketedPoints(func, left, sigma + absChange, shift, Double.POSITIVE_INFINITY);
  }

  private static double solveByBisection(final double optionPrice, final Function1D<Double, double[]> pavFunc, final double lowerSigma,
      final double upperSigma) {
    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(VOL_TOL);
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double volatility) {
        double trialPrice = pavFunc.evaluate(volatility)[0];
        return trialPrice / optionPrice - 1.0;
      }
    };
    return rootFinder.getRoot(func, lowerSigma, upperSigma);
  }

}
