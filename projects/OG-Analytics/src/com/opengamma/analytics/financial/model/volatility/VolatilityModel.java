/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

/**
 * 
 * @param <T> The type of the abscissa(s) 
 */

public interface VolatilityModel<T> {

  Double getVolatility(T t);
}
