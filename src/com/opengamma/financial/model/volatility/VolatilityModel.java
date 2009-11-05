/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

/**
 * 
 * @author emcleod
 * 
 */

public interface VolatilityModel<T> {

  public Double getVolatility(T t);
}
