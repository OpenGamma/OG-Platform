/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * @param <T> Parameter for the probability distribution
 */
public abstract class DistributionParameterEstimator<T> extends Function1D<double[], ProbabilityDistribution<T>> {

}
