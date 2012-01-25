/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.StrikeType;

/**
 * 
 * @param <T> The parameterisation type of the volatility surface
 */
public interface PiecewiseSABRSurfaceFitter1<T extends StrikeType> {

  BlackVolatilitySurface<T> getVolatilitySurface(SmileSurfaceDataBundle data);


}
