/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;

import com.opengamma.engine.viewer.RenderVisitor;
import com.opengamma.engine.viewer.Renderable;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * An {@link AnalyticValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class VolatilitySurfaceAnalyticValue extends AnalyticValueImpl<VolatilitySurface> implements Renderable {
  public VolatilitySurfaceAnalyticValue(VolatilitySurfaceValueDefinition definition, VolatilitySurface value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<VolatilitySurface> scaleForPosition(BigDecimal quantity) {
    return this;
  }

  @Override
  public <T> T accept(RenderVisitor<T> visitor) {
    return visitor.visitVolatilitySurface(getValue());
  }

}
