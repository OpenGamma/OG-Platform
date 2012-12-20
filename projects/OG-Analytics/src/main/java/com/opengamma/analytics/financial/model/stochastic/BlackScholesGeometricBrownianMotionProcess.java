/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.stochastic;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;

/**
 * 
 * @param <T>
 * @param <U>
 */
public class BlackScholesGeometricBrownianMotionProcess<T extends OptionDefinition, U extends StandardOptionDataBundle> extends StochasticProcess<T, U> {

  @Override
  public Function1D<Double, Double> getPathGeneratingFunction(final T t, final U u, final int steps) {
    Validate.notNull(t);
    Validate.notNull(u);
    if (steps < 1) {
      throw new IllegalArgumentException("Number of steps must be greater than zero");
    }
    final double k = t.getStrike();
    final double m = t.getTimeToExpiry(u.getDate());
    final double sigma = u.getVolatility(m, k);
    final double b = u.getCostOfCarry();
    final double dt = m / steps;
    final double sigmaSq = sigma * sigma;
    final double nu = dt * (b - 0.5 * sigmaSq);
    final double sigmaDt = sigma * Math.sqrt(dt);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double e) {
        return nu + sigmaDt * e;
      }
    };
  }

  @Override
  public Double getInitialValue(final T t, final U u) {
    return Math.log(u.getSpot());
  }

  @Override
  public Double getFinalValue(final Double x) {
    return Math.exp(x);
  }

  @Override
  public Function2D<Double, Double> getPathAccumulationFunction() {
    return new Function2D<Double, Double>() {

      @Override
      public Double evaluate(final Double x1, final Double x2) {
        return x1 + x2;
      }

    };
  }
}
