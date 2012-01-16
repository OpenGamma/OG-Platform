/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import com.opengamma.math.function.Function;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 * Interface for volatility calculation.
 */
public interface VolatilityCalculator extends Function<LocalDateDoubleTimeSeries, Double> {

}
