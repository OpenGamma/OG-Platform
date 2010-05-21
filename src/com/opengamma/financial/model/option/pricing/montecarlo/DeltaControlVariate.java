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
 */
public class DeltaControlVariate<T extends OptionDefinition, U extends StandardOptionDataBundle> implements ControlVariate<T, U> {
  protected final AnalyticOptionModel<T, U> _analyticModel;
  protected final Set<Greek> _greek = Collections.singleton(Greek.DELTA);
  protected final double _beta = -1;

  public DeltaControlVariate(final AnalyticOptionModel<T, U> analyticModel) {
    _analyticModel = analyticModel;
  }

  public Function1D<Double, Double> getVariateFunction(final T definition, final U data, final int steps) {
    final double t = definition.getTimeToExpiry(data.getDate());
    final double r = data.getInterestRate(t);
    final double b = data.getCostOfCarry();
    final double dt = t / steps;
    final double df = Math.exp(dt * (r - b));
    final double s = data.getSpot();
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        final double delta = _analyticModel.getGreeks(definition, data, _greek).get(Greek.DELTA);
        return _beta * delta * (x - s * df);
      }

    };
  }

  public Double getInitialValue(final T t, final U u) {
    return 0.;
  }
}
