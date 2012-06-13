/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InitialConditionsProvider {

  //*********************************************************************************************************
  // Backwards PDE initial conditions 
  //*********************************************************************************************************
  /**
   * The payoff of a standard call or put option 
   * @param strike The strike
   * @param isCall true for call
   * @return the payoff function
   */
  public Function1D<Double, Double> getEuropeanPayoff(final double strike, final boolean isCall) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        if (isCall) {
          return Math.max(0, x - strike);
        }
        return Math.max(0, strike - x);
      }
    };
  }

  /**
   * The payoff of a standard call or put option when the spatial variable is the log-spot
   * @param strike The strike
   * @param isCall true for call
   * @return the payoff function
   */
  public Function1D<Double, Double> getLogEuropeanPayoff(final double strike, final boolean isCall) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        final double s = Math.exp(x);
        if (isCall) {
          return Math.max(0, s - strike);
        }
        return Math.max(0, strike - s);
      }
    };
  }

  /**
   * The payoff $\log(S_T)$ where $S_T$ is the price of the underlying at expiry 
   * @return The initial condition for PDE with underlying as spatial variable 
   */
  public Function1D<Double, Double> getLogContractPayoff() {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return Math.log(x);
      }
    };
  }

  /**
   * The payoff $\log(S_T)$ where $S_T$ is the price of the underlying at expiry 
   * @return The initial condition for PDE with log-underlying as spatial variable coordinate 
   */
  public Function1D<Double, Double> getLogContractPayoffInLogCoordinate() {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return x;
      }
    };
  }

  //*********************************************************************************************************
  // forward PDE initial conditions 
  //*********************************************************************************************************
  /**
   * The initial condition for the forward PDE for standard call or put option prices 
   * @param spot The initial level of the underlying
   * @param isCall true for call
   * @return the initial condition 
   */
  public Function1D<Double, Double> getForwardCallPut(final double spot, final boolean isCall) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double k) {
        if (isCall) {
          return Math.max(0, spot - k);
        }
        return Math.max(0, k - spot);
      }
    };
  }

  /**
   * The initial condition for the forward PDE for standard call or put option prices, when the spatial variable is moneyness 
   * @param isCall true for call
   * @return the initial condition as a function of <b>moneyness</b> (strike/spot)
   */
  public Function1D<Double, Double> getForwardCallPut(final boolean isCall) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        if (isCall) {
          return Math.max(0, 1.0 - x);
        }
        return Math.max(0, x - 1.0);
      }
    };
  }

  public Function1D<Double, Double> getLogNormalDensity(final double forward, final double t, final double vol) {
    ArgumentChecker.isTrue(forward > 0, "must have forward > 0");
    ArgumentChecker.isTrue(t > 0, "must have t > 0");
    ArgumentChecker.isTrue(vol > 0, "must have vol > 0");
    final double sigmaRootT = vol * Math.sqrt(t);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward);
        final NormalDistribution dist = new NormalDistribution(0, sigmaRootT);
        return dist.getPDF(x) / s;
      }
    };
  }

  //************
  // Free boundary 
  //***********
  public Surface<Double, Double, Double> getAmericanEarlyExcise(final double strike, final boolean isCall) {
    final Function1D<Double, Double> payoff = getEuropeanPayoff(strike, isCall);
    final Function<Double, Double> temp = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... ts) {
        double s = ts[1];
        return payoff.evaluate(s);
      }
    };
    return FunctionalDoublesSurface.from(temp);
  }

}
