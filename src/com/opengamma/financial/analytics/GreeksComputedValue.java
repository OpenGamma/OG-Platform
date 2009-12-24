/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ComputedValueImpl;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.render.RenderVisitor;
import com.opengamma.financial.render.Renderable;

/**
 * An {@link ComputedValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class GreeksComputedValue extends ComputedValueImpl<GreekResultCollection> implements Renderable {
  public GreeksComputedValue(GreeksResultValueDefinition definition, GreekResultCollection value) {
    super(definition, value);
  }

  @Override
  public ComputedValue<GreekResultCollection> scaleForPosition(BigDecimal quantity) {
    return this;
  }

  @Override
  public <T> T accept(RenderVisitor<T> visitor) {
    return visitor.visitGreekResultCollection(getValue());
  }

}
