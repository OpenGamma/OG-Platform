/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import java.util.Collections;
import java.util.Set;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <T> The option definition type
 * @param <U> The option data bundle type
 * 
 */
public class VegaControlVariate<T extends OptionDefinition, U extends StandardOptionDataBundle> {
  private final AnalyticOptionModel<T, U> _analyticModel;
  private final Set<Greek> _greek = Collections.singleton(Greek.DELTA);
  private final double _beta = -1;

  public VegaControlVariate(final AnalyticOptionModel<T, U> analyticModel) {
    _analyticModel = analyticModel;
  }

  public Function1D<Double, Double> getVariateFunction(final T definition, final U data, final int n) {
    final double t = definition.getTimeToExpiry(data.getDate());
    final double r = data.getInterestRate(t);
    final double b = data.getCostOfCarry();
    final double dt = t / n;
    final double df = Math.exp(dt * (r - b));
    final double s = data.getSpot();
    return new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        final double delta = _analyticModel.getGreeks(definition, data, _greek).get(Greek.DELTA);
        return _beta * delta * (x - s * df);
      }

    };
  }
}
