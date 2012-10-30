/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import com.opengamma.analytics.math.function.Function;

/**
 * 
 * Interface for integration. The function to be integrated can be multi-dimensional. The result
 * of the integration does not have to be the same type as the integration bounds.
 * 
 * @param <T> Type of the function output and result
 * @param <U> Type of the integration bounds
 * @param <V> Type of the function to be integrated (e.g. {@link com.opengamma.analytics.math.function.Function1D}, 
 * {@link com.opengamma.analytics.math.function.FunctionND})
 */
public interface Integrator<T, U, V extends Function<U, T>> {

  /**
   * @param f The function to be integrated, not null
   * @param lower The array of lower bounds of integration, not null or empty
   * @param upper The array of upper bounds of integration, not null or empty 
   * @return The result of the integral
   */
  T integrate(V f, U[] lower, U[] upper);
}
