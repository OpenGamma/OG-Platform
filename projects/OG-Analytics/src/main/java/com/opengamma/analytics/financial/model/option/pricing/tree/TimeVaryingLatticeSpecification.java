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

  private static final Function1D<double[], Double> CALCULATOR = new MeanCalculator();

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    throw new NotImplementedException();
  }

  public double[] getParameters(final double vol, final double nu, final double spaceStep) {
    final double[] res = new double[2];

    final double volSq = vol * vol;
    final double nuSq = nu * nu;
    res[0] = 0.5 * (-volSq + Math.sqrt(volSq * volSq + 4. * nuSq * spaceStep * spaceStep)) / nuSq;
    res[1] = 0.5 + 0.5 * nu * res[0] / spaceStep;

    return res;
  }

  @Override
  public double getTheta(final double spot, final double volatility, final double interestRate, final double dividend, final double dt, final double[] greeksTmp) {
    return 0.5 * (greeksTmp[3] - greeksTmp[0]) / dt;
  }

  public double[] getShiftedDrift(final double[] volatility, final double[] interestRate) {
    final int nSteps = volatility.length;
    final double[] res = new double[nSteps];

    for (int i = 0; i < nSteps; ++i) {
      res[i] = interestRate[i] - 0.5 * volatility[i] * volatility[i];
    }
    return res;
  }

  public double getSpaceStep(final double timeToExpiry, final double[] volatility, final int nSteps, final double[] nu) {
    final double meanNu = CALCULATOR.evaluate(nu);
    final double meanVol = CALCULATOR.evaluate(volatility);
    final double dt = timeToExpiry / nSteps;

    return Math.sqrt(meanVol * meanVol * dt + meanNu * meanNu * dt * dt);
  }
}
