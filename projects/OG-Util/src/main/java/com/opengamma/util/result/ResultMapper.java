/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

/**
 * Functional interface that can transform a result.
 *
 * @param <R> the result type
 * @param <T> the type of the transformed result
 */
public interface ResultMapper<R, T> {

  /**
   * Transforms the input.
   *
   * @param result the result to be transformed
   * @return the transformed result, not null
   */
  Result<T> map(R result);
}
