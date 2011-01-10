/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <T>
 * @param <U>
 * @param <V>
 */
public abstract class Integrator1D<T, U, V> implements Integrator<T, Function1D<V, V>, V> {
  private static final Logger s_logger = LoggerFactory.getLogger(Integrator1D.class);

  public T integrate(final U f, final V[] lower, final V[] upper) {
    if (f == null) {
      throw new IllegalArgumentException("Function was null");
    }
    if (lower == null) {
      throw new IllegalArgumentException("Lower bound array was null");
    }
    if (lower.length == 0) {
      throw new IllegalArgumentException("Lower bound array was empty");
    }
    if (upper == null) {
      throw new IllegalArgumentException("Upper bound array was null");
    }
    if (upper.length == 0) {
      throw new IllegalArgumentException("Upper bound array was empty");
    }
    if (lower.length > 1) {
      s_logger.info("Lower bound array had more than one element; only using the first");
    }
    if (lower[0] == null) {
      throw new IllegalArgumentException("Lower bound was null");
    }
    if (upper.length > 1) {
      s_logger.info("Upper bound array had more than one element; only using the first");
    }
    if (upper[0] == null) {
      throw new IllegalArgumentException("Upper bound was null");
    }
    return integrate(f, lower[0], upper[0]);
  }

  public abstract T integrate(U f, V lower, V upper);

}
