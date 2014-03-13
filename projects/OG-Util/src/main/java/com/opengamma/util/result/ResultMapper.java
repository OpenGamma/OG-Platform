/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import com.google.common.base.Function;

/**
 * Functional interface that can transform a result.
 *
 * @param <R> the result type
 * @param <T> the type of the transformed result
 * @deprecated use a general purpose function type and {@link Result#ifSuccess(Function)} or
 * {@link Result#flatMap(Function)}
 */
@Deprecated
public interface ResultMapper<R, T> {

  /**
   * Transforms the input.
   *
   * @param result the result to be transformed
   * @return the transformed result, not null
   */
  Result<T> map(R result);
}
