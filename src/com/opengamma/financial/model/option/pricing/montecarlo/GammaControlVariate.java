/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.montecarlo;

import java.util.Collections;
import java.util.Set;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class GammaControlVariate<T extends OptionDefinition, U extends StandardOptionDataBundle> implements ControlVariate<T, U> {
  protected final AnalyticOptionModel<T, U> _analyticModel;
  protected final Set<Greek> _greek = Collections.singleton(Greek.GAMMA);
  protected final double _beta = -0.5;

  public GammaControlVariate(final AnalyticOptionModel<T, U> analyticModel) {
    _analyticModel = analyticModel;
  }

  @Override
  public Function1D<Double, Double> getVariateFunction(final T definition, final U data, final int steps) {
    final double t = definition.getTimeToExpiry(data.getDate());
    final double r = data.getInterestRate(t);
    final double b = data.getCostOfCarry();
    final double sigma = data.getVolatility(t, definition.getStrike());
    final double dt = t / steps;
    final double df1 = Math.exp(dt * (r - b));
    final double df2 = df1 * (df1 * Math.exp(sigma * sigma * dt) - 2) + 1;
    final double s = data.getSpot();
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        final double gamma = (Double) _analyticModel.getGreeks(definition, data, _greek).get(Greek.GAMMA).getResult();
        final double diff = x - s;
        return _beta * gamma * (diff * diff - s * s * df2);
      }

    };
  }

  public Double getInitialValue(final T t, final U u) {
    return 0.;
  }
}
