/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;

import com.opengamma.engine.analytics.ComputedValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.ComputedValueImpl;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.render.RenderVisitor;
import com.opengamma.financial.render.Renderable;

/**
 * An {@link ComputedValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class DiscountCurveComputedValue extends ComputedValueImpl<DiscountCurve> implements Renderable {
  public DiscountCurveComputedValue(AnalyticValueDefinition<DiscountCurve> definition, DiscountCurve value) {
    super(definition, value);
  }

  @Override
  public ComputedValue<DiscountCurve> scaleForPosition(BigDecimal quantity) {
    return new DiscountCurveComputedValue(getDefinition(), (DiscountCurve) getValue());
  }

  @Override
  public <T> T accept(RenderVisitor<T> visitor) {
    return visitor.visitDiscountCurve((InterpolatedDiscountCurve)getValue());
  }

}
