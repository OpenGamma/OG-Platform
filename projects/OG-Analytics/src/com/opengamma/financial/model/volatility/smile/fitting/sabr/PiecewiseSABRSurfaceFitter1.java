/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;

/**
 * 
 */
public interface PiecewiseSABRSurfaceFitter1 {

  BlackVolatilitySurface getVolatilitySurface(SmileSurfaceDataBundle data);

}
