/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import com.opengamma.math.function.Function;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * Interface for volatility calculation.
 */
public interface VolatilityCalculator extends Function<DoubleTimeSeries<?>, Double> {

}
