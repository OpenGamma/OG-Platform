/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Collections;
import java.util.Map;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator2D;

/**
 * 
 * @author emcleod
 */
public class StrikeTimeToExpiryInterpolatedVolatilitySurfaceModel<T extends OptionDefinition<?>, U extends StandardOptionDataBundle> extends
    InterpolatedVolatilitySurfaceModel<T, U> {
  private final Function1D<Map.Entry<T, U>, Double> _xAxisFunction = new Function1D<Map.Entry<T, U>, Double>() {

    @Override
    public Double evaluate(final Map.Entry<T, U> x) {
      final T definition = x.getKey();
      final U dataBundle = x.getValue();
      return definition.getTimeToExpiry(dataBundle.getDate());
    }

  };
  private final Function1D<T, Double> _yAxisFunction = new Function1D<T, Double>() {

    @Override
    public Double evaluate(final T x) {
      return x.getStrike();
    }

  };

  public StrikeTimeToExpiryInterpolatedVolatilitySurfaceModel(final Interpolator2D interpolator) {
    super(interpolator);
  }

  @Override
  protected Interpolator2D getInterpolator(final Interpolator2D interpolator) {
    return new VolatilityInterpolator2D(interpolator);
  }

  @Override
  protected Double getXAxisFunctionValue(final T t, final U u) {
    final Map<T, U> map = Collections.<T, U> singletonMap(t, u);
    return _xAxisFunction.evaluate(map.entrySet().iterator().next());
  }

  @Override
  protected Double getYAxisFunctionValue(final T t, final U u) {
    return _yAxisFunction.evaluate(t);
  }
}
