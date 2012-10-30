/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

/**
 * 
 * @param <T> The type of the data to roll down
 */
public interface RolldownFunction<T> {

  T rollDown(T data, double time);
}
