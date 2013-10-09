/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;

/**
 * 
 */
public class TimeVaryingLatticeSpecification extends LatticeSpecification {

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    throw new NotImplementedException();
  }

  /**
   * Overloaded getParameters method 
   * @param vol Volatility
   * @param nu Computed by getShiftedDrift method
   * @param spaceStep Space step
   * @return {(modified time step), (up probability)} 
   */
  public double[] getParameters(final double vol, final double nu, final double spaceStep) {
    final double[] res = new double[2];

    final double volSq = vol * vol;
    final double nuSq = nu * nu;
    res[0] = 0.5 * (-volSq + Math.sqrt(volSq * volSq + 4. * nuSq * spaceStep * spaceStep)) / nuSq;
    res[1] = 0.5 + 0.5 * nu * res[0] / spaceStep;

    return res;
  }

  /**
   * Parameters for trinomial tree
   * @param vol Volatility
   * @param nu Computed by getShiftedDrift method
   * @param dt Time step
   * @param spaceStep Homogeneous space step
   * @return {(up probability), (middle probability), (down probability)}
   */
  public double[] getParametersTrinomial(final double vol, final double nu, final double dt, final double spaceStep) {
    final double nudt = nu * dt;
    final double dx = vol * Math.sqrt(3. * dt);

    final double part = (vol * vol * dt + nudt * nudt) / dx / dx;
    final double upProbability = 0.5 * (part + nudt / dx);
    final double middleProbability = 1. - part;
    final double downProbability = 0.5 * (part - nudt / dx);

    return new double[] {upProbability, middleProbability, downProbability };
  }

  @Override
  public double getTheta(final double spot, final double volatility, final double interestRate, final double dividend, final double dt, final double[] greeksTmp) {
    return 0.5 * (greeksTmp[3] - greeksTmp[0]) / dt;
  }

  /**
   * Finite difference approximation of theta. Note that the time step is not homogeneous in the time varying volatility lattice. 
   * @param dt0 First time step
   * @param dt1 Second time step
   * @param greeksTmp asset price at (0,0) in  greeksTmp[0] and asset price at (2,1) in greeksTmp[3]
   * @return Option theta 
   */
  public double getTheta(final double dt0, final double dt1, final double[] greeksTmp) {
    return (greeksTmp[3] - greeksTmp[0]) / (dt0 + dt1);
  }

  /**
   * @param volatility Volatility
   * @param interestRate Interest rate
   * @param dividend Dividend
   * @return (interest rate) - (dividend) - 0.5 * volatility * volatility for all layers 
   */
  public double[] getShiftedDrift(final double[] volatility, final double[] interestRate, final double[] dividend) {
    final int nSteps = volatility.length;
    final double[] res = new double[nSteps];

    for (int i = 0; i < nSteps; ++i) {
      res[i] = interestRate[i] - dividend[i] - 0.5 * volatility[i] * volatility[i];
    }
    return res;
  }

  /**
   * @param timeToExpiry Time to expiry
   * @param volatility Volatility
   * @param nSteps Number of steps
   * @param nu Computed by getShiftedDrift method
   * @return space step 
   */
  public double getSpaceStep(final double timeToExpiry, final double[] volatility, final int nSteps, final double[] nu) {
    final Function1D<double[], Double> calculator = new MeanCalculator();
    final double meanNu = calculator.evaluate(nu);
    final double meanVol = calculator.evaluate(volatility);
    final double dt = timeToExpiry / nSteps;

    return Math.sqrt(meanVol * meanVol * dt + meanNu * meanNu * dt * dt);
  }

  /**
   * @param volatility Volatility
   * @param nu Computed by getShiftedDrift method
   * @param dt time step
   * @return space step 
   */
  public double getSpaceStepTrinomial(final double[] volatility, final double[] nu, final double dt) {
    final Function1D<double[], Double> calculator = new MeanCalculator();
    final double meanNu = calculator.evaluate(nu);
    final double meanVol = calculator.evaluate(volatility);

    return Math.sqrt(3. * meanVol * meanVol * dt + meanNu * meanNu * dt * dt);
  }
}
