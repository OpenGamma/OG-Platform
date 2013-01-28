/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

/**
 * Provides a value from a security for displaying in a cell in the blotter grid.
 * @param <T> The security type
 */
public interface CellValueProvider<T> {

  /**
   * Returns a value for a security.
   * @param security The security
   * @return The value
   */
  Object getValue(T security);
}
