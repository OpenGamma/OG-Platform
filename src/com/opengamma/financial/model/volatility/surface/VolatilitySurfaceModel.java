/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;

public interface VolatilitySurfaceModel<T, U> {

  public VolatilitySurface getSurface(Map<T, Double> optionData, U data);
}
