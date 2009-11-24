/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.stochastic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.random.RandomNumberGenerator;

/**
 * 
 * @author emcleod
 */
public class BlackScholesGeometricBrownianMotionProcess<T extends OptionDefinition, U extends StandardOptionDataBundle> implements StochasticProcess<T, U> {
  private final RandomNumberGenerator _generator;

  public BlackScholesGeometricBrownianMotionProcess(final RandomNumberGenerator generator) {
    if (generator == null)
      throw new IllegalArgumentException("Generator was null");
    _generator = generator;
  }

  @Override
  public List<Double[]> getPath(final T t, final U u, final int n, final int steps) {
    if (t == null)
      throw new IllegalArgumentException("Option definition was null");
    if (u == null)
      throw new IllegalArgumentException("Data bundle was null");
    if (n < 1)
      throw new IllegalArgumentException("Asked for " + n + " paths; one is the minimum");
    if (steps < 1)
      throw new IllegalArgumentException("Asked for " + steps + " steps; one is the minimum");
    final double x = Math.log(u.getSpot());
    final double k = t.getStrike();
    final double m = t.getTimeToExpiry(u.getDate());
    final double sigma = u.getVolatility(m, k);
    final double r = u.getInterestRate(m);
    final double b = u.getCostOfCarry();
    final double dt = m / n;
    final double sigmaSq = sigma * sigma * Math.sqrt(m);
    final double nu = dt * (r - b) - 0.5 * sigmaSq;
    final List<Double[]> randomNumbers = _generator.getVectors(steps, n);
    final List<Double[]> result = new ArrayList<Double[]>();
    Double[] y, random;
    final Iterator<Double[]> iter = randomNumbers.iterator();
    for (int i = 0; i < n; i++) {
      y = new Double[steps];
      random = iter.next();
      y[0] = x + nu + sigmaSq * random[0];
      for (int j = 1; j < steps; j++) {
        y[j] = y[j - 1] + nu + sigmaSq * random[j];
      }
      result.add(y);
    }
    return result;
  }
}
