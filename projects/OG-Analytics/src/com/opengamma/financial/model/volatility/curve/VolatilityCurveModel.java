/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.curve;

/**
 * 
 * @param <T> Type of market data
 * @param <U> Type of other data needed to produce the curve
 */
public interface VolatilityCurveModel<T, U> {

  VolatilityCurve getCurve(T marketData, U data);
}
