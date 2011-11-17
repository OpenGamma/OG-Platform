/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import org.apache.commons.lang.Validate;

import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * This <b>SHOULD</b> be the repository for Black formulas - i.e. the price, common greeks (delta, gamma, vega) and implied volatility. Other
 * classes that have higher level abstractions (e.g. option data bundles) should call these functions. 
 * As the numeraire (e.g. the zero bond p(0,T) in the T-forward measure) in the Black formula is just a multiplication factor,  all prices,
 * input/output, are <b>forward</b> prices, i.e. (spot price)/numeraire  
 */
public abstract class BlackFormulaRepository {

  private static final double EPS = 1e-15;
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double SMALL = 1.0E-12;
  private static final int MAX_ITERATIONS = 15; //something's wrong if Newton-Raphson taking longer than this 
  private static final double VOL_TOL = 1e-9; // 1 part in 100,000 basis points will do for implied vol 

  /**
   * The <b>forward</b> price of an option using the Black formula  
   * @param forward The forward value of the underlying 
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility 
   * @param isCall True for calls, false for puts 
   * @return The <b>forward</b> price
   */
  @ExternalFunction
  public static double price(final double forward, final double strike, final double timeToExpiry, final double lognormalVol, final boolean isCall) {
    Validate.isTrue(lognormalVol >= 0.0, "negative vol");
    if (strike < SMALL) {
      return isCall ? forward : 0.0;
    }
    final int sign = isCall ? 1 : -1;
    final double sigmaRootT = lognormalVol * Math.sqrt(timeToExpiry);
    if (Math.abs(forward - strike) < SMALL) {
      return forward * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1);
    }
    if (sigmaRootT < SMALL) {
      return Math.max(sign * (forward - strike), 0.0);
    }

    final double d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    final double d2 = d1 - sigmaRootT;
    return sign * (forward * NORMAL.getCDF(sign * d1) - strike * NORMAL.getCDF(sign * d2));

  }

  public static double price(final SimpleOptionData data, final double lognormalVol) {
    return data.getDiscountFactor() * price(data.getForward(), data.getStrike(), data.getTimeToExpiry(), lognormalVol, data.isCall());
  }

  //TODO other greeks 

  /**
   * The forward vega of an option, i.e. the sensitivity of the option's forward price wrt the implied volatility (which is just the the spot vega
   * divide by the the numeraire) 
   * @param forward The forward value of the underlying 
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param lognormalVol The log-normal volatility 
   * @return The forward vega
   */
  public static double vega(final double forward, final double strike, final double timeToExpiry, final double lognormalVol) {
    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;

    if (Math.abs(forward - strike) < SMALL) {
      return forward * rootT * NORMAL.getPDF(sigmaRootT / 2);
    }

    if (sigmaRootT < SMALL || strike < SMALL) {
      return 0.0;
    }

    final double d1 = Math.log(forward / strike) / lognormalVol / rootT + 0.5 * lognormalVol * rootT;
    return forward * rootT * NORMAL.getPDF(d1);
  }

  @ExternalFunction
  public static double vega(final SimpleOptionData data, final double lognormalVol) {
    return data.getDiscountFactor() * vega(data.getForward(), data.getStrike(), data.getTimeToExpiry(), lognormalVol);
  }

  /**
   * Get the log-normal (Black) implied volatility of a European option 
   * @param price The <b>forward</b> price - i.e. the market price divided by the numeraire (i.e. the zero bond p(0,T) for the T-forward measure)
   * @param forward The forward value of the underlying 
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @param isCall  True for calls, false for puts 
   * @return log-normal (Black) implied volatility
   */
  public static double impliedVolatility(final double price, final double forward, final double strike, final double timeToExpiry, final boolean isCall) {

    final double intrinsicPrice = Math.max(0, (isCall ? 1 : -1) * (forward - strike));
    Validate.isTrue(strike > 0, "Cannot find an implied volatility when strike is zero as there is no optionality");
    Validate.isTrue(price >= intrinsicPrice, "option price (" + price + ") less than intrinsic value (" + intrinsicPrice
        + ")");
    if (isCall) {
      Validate.isTrue(price < forward, "call price must be less than forward");
    } else {
      Validate.isTrue(price < strike, "put price must be less than strike");
    }

    if (price == intrinsicPrice) {
      return 0.0;
    }

    double sigma = 0.3;
    double lowerSigma;
    double upperSigma;

    try {
      final double[] temp = bracketRoot(price, forward, strike, timeToExpiry, isCall, sigma, 0.1);
      lowerSigma = temp[0];
      upperSigma = temp[1];
    } catch (MathException e) {
      throw new IllegalArgumentException(e.toString() + " No implied Volatility for this price. [price: " + price + ", forward: " + forward + ", strike: " +
          strike + ", timeToExpiry: " + timeToExpiry + ", " + (isCall ? "Call" : "put"));
    }

    final double maxChange = 0.5;

    double[] pnv = priceAndVega(forward, strike, timeToExpiry, sigma, isCall);
    //TODO check if this is ever called 
    if (pnv[1] == 0 || Double.isNaN(pnv[1])) {
      return solveByBisection(price, forward, strike, timeToExpiry, isCall, lowerSigma, upperSigma);
    }
    double diff = pnv[0] - price;
    boolean above = diff > 0;
    if (above) {
      upperSigma = sigma;
    } else {
      lowerSigma = sigma;
    }

    double trialChange = -diff / pnv[1];
    double actChange;
    if (trialChange > 0.0) {
      actChange = Math.min(maxChange, Math.min(trialChange, upperSigma - sigma));
    } else {
      actChange = Math.max(-maxChange, Math.max(trialChange, lowerSigma - sigma));
    }
    //
    //    double sign = Math.signum(trialChange);
    //    trialChange = sign * Math.min(maxChange, Math.abs(trialChange));
    //    if (trialChange > 0 && trialChange > sigma) {
    //      trialChange = sigma;
    //    }
    int count = 0;
    while (Math.abs(actChange) > VOL_TOL) {
      sigma += actChange;

      pnv = priceAndVega(forward, strike, timeToExpiry, sigma, isCall);

      if (pnv[1] == 0 || Double.isNaN(pnv[1])) {
        return solveByBisection(price, forward, strike, timeToExpiry, isCall, lowerSigma, upperSigma);
      }

      diff = pnv[0] - price;
      above = diff > 0;
      if (above) {
        upperSigma = sigma;
      } else {
        lowerSigma = sigma;
      }

      trialChange = -diff / pnv[1];
      if (trialChange > 0.0) {
        actChange = Math.min(maxChange, Math.min(trialChange, upperSigma - sigma));
      } else {
        actChange = Math.max(-maxChange, Math.max(trialChange, lowerSigma - sigma));
      }

      //      sign = Math.signum(trialChange);
      //      trialChange = sign * Math.min(maxChange, Math.abs(trialChange));
      //      if (trialChange > 0 && trialChange > sigma) {
      //        trialChange = sigma;
      //      }
      if (count++ > MAX_ITERATIONS) {
        return solveByBisection(price, forward, strike, timeToExpiry, isCall, lowerSigma, upperSigma);
      }
    }
    return sigma;
  }

  public static double impliedVolatility(final SimpleOptionData data, final double price) {
    return impliedVolatility(price / data.getDiscountFactor(), data.getForward(), data.getStrike(), data.getTimeToExpiry(), data.isCall());
  }

  public static double impliedVolatility(final SimpleOptionData[] data, final double price) {
    Validate.notEmpty(data, "no option data given");
    double intrinsicPrice = 0.0;
    for (SimpleOptionData option : data) {
      intrinsicPrice += Math.max(0, (option.isCall() ? 1 : -1) * option.getDiscountFactor() * (option.getForward() - option.getStrike()));
    }
    Validate.isTrue(price >= intrinsicPrice, "option price (" + price + ") less than intrinsic value (" + intrinsicPrice
        + ")");

    if (price == intrinsicPrice) {
      return 0.0;
    }
    double sigma = 0.3;

    final double maxChange = 0.5;

    double modelPrice = 0.0;
    double vega = 0.0;
    for (SimpleOptionData option : data) {
      modelPrice += price(option, sigma);
      vega += vega(option, sigma);
    }

    if (vega == 0 || Double.isNaN(vega)) {
      return solveByBisection(data, price, sigma, 0.1);
    }
    double change = (modelPrice - price) / vega;
    double previousChange = 0.0;

    double sign = Math.signum(change);
    change = sign * Math.min(maxChange, Math.abs(change));
    if (change > 0 && change > sigma) {
      change = sigma;
    }
    int count = 0;
    while (Math.abs(change) > VOL_TOL) {
      sigma -= change;

      modelPrice = 0.0;
      vega = 0.0;
      for (SimpleOptionData option : data) {
        modelPrice += price(option, sigma);
        vega += vega(option, sigma);
      }

      if (vega == 0 || Double.isNaN(vega)) {
        return solveByBisection(data, price, sigma, 0.1);
      }
      change = (modelPrice - price) / vega;
      sign = Math.signum(change);
      change = sign * Math.min(maxChange, Math.abs(change));
      if (change > 0 && change > sigma) {
        change = sigma;
      }

      //detect oscillation around the solution 
      if (count > 5 && Math.abs(previousChange + change) < VOL_TOL) {
        change /= 2.0;
      }
      previousChange = change;

      if (count++ > MAX_ITERATIONS) {
        return solveByBisection(data, price, sigma, change);
      }
    }
    return sigma;
  }

  private static double[] priceAndVega(final double forward, final double strike, final double timeToExpiry, final double lognormalVol, final boolean isCall) {

    final double rootT = Math.sqrt(timeToExpiry);
    final double sigmaRootT = lognormalVol * rootT;
    double[] res = new double[2];

    if (Math.abs(forward - strike) < SMALL) {
      res[0] = forward * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1);
      res[1] = forward * rootT * NORMAL.getPDF(sigmaRootT / 2);
      return res;
    }

    final int sign = isCall ? 1 : -1;

    if (sigmaRootT < SMALL || strike < SMALL) {
      res[0] = Math.max(sign * (forward - strike), 0.0);
      res[1] = 0.0;
      return res;
    }

    final double d1 = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    final double d2 = d1 - sigmaRootT;
    res[0] = sign * (forward * NORMAL.getCDF(sign * d1) - strike * NORMAL.getCDF(sign * d2));
    res[1] = forward * rootT * NORMAL.getPDF(d1);
    return res;
  }

  private static double[] bracketRoot(final double forwardPrice, final double forward, final double strike, final double expiry, final boolean isCall, final double sigma, final double change) {
    final BracketRoot bracketer = new BracketRoot();
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @SuppressWarnings({"synthetic-access" })
      @Override
      public Double evaluate(final Double volatility) {
        return price(forward, strike, expiry, volatility, isCall) - forwardPrice;
      }
    };
    return bracketer.getBracketedPoints(func, sigma - Math.abs(change), sigma + Math.abs(change), 0, Double.POSITIVE_INFINITY);
  }

  private static double solveByBisection(final double forwardPrice, final double forward, final double strike, final double expiry, final boolean isCall, final double lowerSigma,
      final double upperSigma) {
    //    final BracketRoot bracketer = new BracketRoot();
    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(1e-6); //0.01bps accuracy 
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @SuppressWarnings({"synthetic-access" })
      @Override
      public Double evaluate(final Double volatility) {
        return price(forward, strike, expiry, volatility, isCall) - forwardPrice;
      }
    };
    // final double[] range = bracketer.getBracketedPoints(func, sigma - Math.abs(change), sigma + Math.abs(change));
    //  try {
    return rootFinder.getRoot(func, lowerSigma, upperSigma);
    //    } catch (Exception e) {
    //
    //      final double[] temp = bracketRoot(forwardPrice, forward, strike, expiry, isCall, 0.3, 0.1);
    //      return 0.0;
    //    }
  }

  private static double solveByBisection(final SimpleOptionData[] data, final double price, final double sigma, final double change) {
    final BracketRoot bracketer = new BracketRoot();
    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(EPS);
    final int n = data.length;
    final Function1D<Double, Double> func = new Function1D<Double, Double>() {

      @SuppressWarnings({"synthetic-access" })
      @Override
      public Double evaluate(final Double volatility) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
          sum += price(data[i], volatility);
        }
        return sum - price;
      }
    };
    final double[] range = bracketer.getBracketedPoints(func, sigma - Math.abs(change), sigma + Math.abs(change));
    return rootFinder.getRoot(func, range[0], range[1]);
  }

}
