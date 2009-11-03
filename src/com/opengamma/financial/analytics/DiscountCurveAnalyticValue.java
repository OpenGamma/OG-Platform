/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueImpl;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.render.RenderVisitor;
import com.opengamma.financial.render.Renderable;

/**
 * An {@link AnalyticValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class DiscountCurveAnalyticValue extends AnalyticValueImpl<DiscountCurve> implements Renderable {
  public DiscountCurveAnalyticValue(AnalyticValueDefinition<DiscountCurve> definition, DiscountCurve value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<DiscountCurve> scaleForPosition(BigDecimal quantity) {
    return new DiscountCurveAnalyticValue(getDefinition(), (DiscountCurve) getValue());
  }

  @Override
  public <T> T accept(RenderVisitor<T> visitor) {
    return visitor.visitDiscountCurve((InterpolatedDiscountCurve)getValue());
  }

}
