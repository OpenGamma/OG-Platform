/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.stochastic;

import java.util.List;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.OptionPayoffFunction;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class BlackScholesGeometricBrownianMotionProcess<T extends OptionDefinition, U extends StandardOptionDataBundle> implements StochasticProcess<T, U> {

  @Override
  public Function1D<Double[], Double[]> getPathGeneratingFunction(final T t, final U u, final int steps) {
    if (t == null)
      throw new IllegalArgumentException("Option definition was null");
    if (u == null)
      throw new IllegalArgumentException("Data bundle was null");
    if (steps < 1)
      throw new IllegalArgumentException("Number of steps must be greater than zero");
    final double x = Math.log(u.getSpot());
    final double k = t.getStrike();
    final double m = t.getTimeToExpiry(u.getDate());
    final double sigma = u.getVolatility(m, k);
    final double r = u.getInterestRate(m);
    final double b = u.getCostOfCarry();
    final double dt = m / steps;
    final double sigmaSq = sigma * sigma;
    final double nu = dt * (r - b - 0.5 * sigmaSq);
    final double sigmaDt = sigma * Math.sqrt(dt);
    return new Function1D<Double[], Double[]>() {

      @Override
      public Double[] evaluate(final Double[] e) {
        final Double[] y = new Double[steps];
        y[0] = x + nu + sigmaDt * e[0];
        for (int i = 1; i < steps; i++) {
          y[i] = y[i - 1] + nu + sigmaDt * e[i];
        }
        return y;
      }

    };
  }

  @Override
  public Function1D<List<Double[]>, Double> getValue(final OptionDefinition definition, final StandardOptionDataBundle data, final int steps) {
    final OptionPayoffFunction<StandardOptionDataBundle> payoffFunction = definition.getPayoffFunction();
    final double t = definition.getTimeToExpiry(data.getDate());
    final double df = data.getDiscountCurve().getDiscountFactor(t);
    return new Function1D<List<Double[]>, Double>() {

      @Override
      public Double evaluate(final List<Double[]> paths) {
        double sum = 0;
        for (final Double[] path : paths) {
          sum += payoffFunction.getPayoff(data.withSpot(Math.exp(path[steps - 1])), 0.);
        }
        System.out.println(df * sum);
        return df * sum / paths.size();
      }

    };
  }
}
