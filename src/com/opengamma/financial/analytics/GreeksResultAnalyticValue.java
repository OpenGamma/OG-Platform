/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueImpl;
import com.opengamma.engine.viewer.RenderVisitor;
import com.opengamma.engine.viewer.Renderable;
import com.opengamma.financial.greeks.GreekResultCollection;

/**
 * An {@link AnalyticValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class GreeksResultAnalyticValue extends AnalyticValueImpl<GreekResultCollection> implements Renderable {
  public GreeksResultAnalyticValue(GreeksResultValueDefinition definition, GreekResultCollection value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<GreekResultCollection> scaleForPosition(BigDecimal quantity) {
    return this;
  }

  @Override
  public <T> T accept(RenderVisitor<T> visitor) {
    return visitor.visitGreekResultCollection(getValue());
  }

}
