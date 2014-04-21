/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

/**
 * Rolls down market data. Possible implementations could be a function that rolls down
 * market data with or without slide.
 *
 * @param <T> The type of the data to roll down
 */
public interface RolldownFunction<T> {

  /**
   * @param data The market data, not null
   * @param time The amount of time in <b>years<b> that should be rolled forward.
   * @return Market data that has been rolled down.
   */
  T rollDown(T data, double time);
}
