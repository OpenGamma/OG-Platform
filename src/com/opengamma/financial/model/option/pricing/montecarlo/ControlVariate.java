/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.montecarlo;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public interface ControlVariate<T, U> {

  public Function1D<Double, Double> getVariateFunction(T t, U u, int steps);

  public Double getInitialValue(T t, U u);
}
