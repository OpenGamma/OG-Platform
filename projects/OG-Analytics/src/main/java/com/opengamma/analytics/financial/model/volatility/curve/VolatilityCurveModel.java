/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

/**
 * 
 * @param <T> Type of market data
 * @param <U> Type of other data needed to produce the curve
 */
public interface VolatilityCurveModel<T, U> {

  VolatilityCurve getCurve(T marketData, U data);
}
