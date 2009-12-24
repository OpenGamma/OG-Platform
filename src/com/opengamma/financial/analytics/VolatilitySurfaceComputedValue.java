/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;

import com.opengamma.engine.analytics.ComputedValue;
import com.opengamma.engine.analytics.ComputedValueImpl;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.render.RenderVisitor;
import com.opengamma.financial.render.Renderable;

/**
 * An {@link ComputedValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class VolatilitySurfaceComputedValue extends ComputedValueImpl<VolatilitySurface> implements Renderable {
  public VolatilitySurfaceComputedValue(VolatilitySurfaceValueDefinition definition, VolatilitySurface value) {
    super(definition, value);
  }

  @Override
  public ComputedValue<VolatilitySurface> scaleForPosition(BigDecimal quantity) {
    return this;
  }

  @Override
  public <T> T accept(RenderVisitor<T> visitor) {
    return visitor.visitVolatilitySurface(getValue());
  }

}
