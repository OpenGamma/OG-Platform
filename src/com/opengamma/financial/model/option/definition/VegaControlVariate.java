/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import java.util.Arrays;
import java.util.List;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class VegaControlVariate<T extends OptionDefinition, U extends StandardOptionDataBundle> {
  protected final AnalyticOptionModel<T, U> _analyticModel;
  protected final List<Greek> _greek = Arrays.asList(Greek.DELTA);
  protected final double _beta = -1;

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

      @Override
      public Double evaluate(final Double x) {
        final double delta = (Double) _analyticModel.getGreeks(definition, data, _greek).get(Greek.DELTA).getResult();
        return _beta * delta * (x - s * df);
      }

    };
  }
}
