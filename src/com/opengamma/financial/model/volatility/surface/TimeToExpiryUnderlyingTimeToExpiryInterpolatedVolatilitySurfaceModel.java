/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Collections;
import java.util.Map;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardFixedIncomeUnderlyingOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator2D;

/**
 * 
 * @author emcleod
 */
public class TimeToExpiryUnderlyingTimeToExpiryInterpolatedVolatilitySurfaceModel<T extends OptionDefinition> extends
    InterpolatedVolatilitySurfaceModel<T, StandardFixedIncomeUnderlyingOptionDataBundle> {
  private final Function1D<Map.Entry<T, StandardFixedIncomeUnderlyingOptionDataBundle>, Double> _xAxisFunction = new Function1D<Map.Entry<T, StandardFixedIncomeUnderlyingOptionDataBundle>, Double>() {

    @Override
    public Double evaluate(final Map.Entry<T, StandardFixedIncomeUnderlyingOptionDataBundle> x) {
      final T definition = x.getKey();
      final StandardFixedIncomeUnderlyingOptionDataBundle dataBundle = x.getValue();
      return definition.getTimeToExpiry(dataBundle.getDate());
    }

  };
  private final Function1D<StandardFixedIncomeUnderlyingOptionDataBundle, Double> _yAxisFunction = new Function1D<StandardFixedIncomeUnderlyingOptionDataBundle, Double>() {

    @Override
    public Double evaluate(final StandardFixedIncomeUnderlyingOptionDataBundle x) {
      return x.getUnderlyingInstrument().getTenor(x.getDate());
    }

  };

  public TimeToExpiryUnderlyingTimeToExpiryInterpolatedVolatilitySurfaceModel(final Interpolator2D interpolator) {
    super(interpolator);
  }

  @Override
  protected Double getXAxisFunctionValue(final T t, final StandardFixedIncomeUnderlyingOptionDataBundle u) {
    final Map<T, StandardFixedIncomeUnderlyingOptionDataBundle> map = Collections.<T, StandardFixedIncomeUnderlyingOptionDataBundle> singletonMap(t, u);
    return _xAxisFunction.evaluate(map.entrySet().iterator().next());
  }

  @Override
  protected Double getYAxisFunctionValue(final T t, final StandardFixedIncomeUnderlyingOptionDataBundle u) {
    return _yAxisFunction.evaluate(u);
  }
}
