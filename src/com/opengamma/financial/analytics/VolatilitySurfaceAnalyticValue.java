/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueImpl;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.render.RenderVisitor;
import com.opengamma.financial.render.Renderable;

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
