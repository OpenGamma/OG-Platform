/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import java.util.List;

/**
 * interface for anything the provides a vector function which depends on some extraneous data
 * @param <T> type of extraneous data 
 * @see {@link VectorFunction}
 *   */
public interface VectorFunctionProvider<T> {

  /**
   * produce a vector function that depends in some way on the given extraneous data
   * @param data List of data points 
   * @return a {@link VectorFunction}
   */
  VectorFunction from(final List<T> data);

  /**
   * produce a vector function that depends in some way on the given extraneous data
   * @param data Array of data points
   * @return a {@link VectorFunction}
   */
  VectorFunction from(final T[] data);

}
