/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

/**
 * 
 * @param <T>
 * @param <U>
 */
public interface VolatilitySurfaceModel<T, U> {

  VolatilitySurface getSurface(T marketData, U data);
}
