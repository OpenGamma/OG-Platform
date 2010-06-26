/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;

/**
 * 
 * @param <T>
 * @param <U>
 */
public interface VolatilitySurfaceModel<T, U> {

  VolatilitySurface getSurface(Map<T, Double> optionData, U data);
}
