/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.Pair;

/**
 * for a model of forward rates that follow the SDE df = a(f,t)dt + b(f,t)dw this describes the drift function (of forward, f, and time, t)
 */
public class DriftSurface {

  Surface<Double, Double, Double> _surface;

  public DriftSurface(final Surface<Double, Double, Double> surface) {
    Validate.notNull(surface, "surface");
    _surface = surface;
  }


  public double getDrift(final double f, final double t) {
    return _surface.getZValue(f, t);
  }

}
