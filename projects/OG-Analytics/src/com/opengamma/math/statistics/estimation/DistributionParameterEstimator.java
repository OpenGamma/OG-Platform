/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @param <T> Parameter for the probability distribution
 */
public abstract class DistributionParameterEstimator<T> extends Function1D<double[], ProbabilityDistribution<T>> {

}
