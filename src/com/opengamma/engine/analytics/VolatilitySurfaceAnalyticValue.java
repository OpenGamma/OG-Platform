/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;

import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * An {@link AnalyticValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class VolatilitySurfaceAnalyticValue extends AbstractAnalyticValue<VolatilitySurface> {
  public VolatilitySurfaceAnalyticValue(VolatilitySurfaceValueDefinition definition, VolatilitySurface value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<VolatilitySurface> scaleForPosition(BigDecimal quantity) {
    throw new UnsupportedOperationException();
  }

}
