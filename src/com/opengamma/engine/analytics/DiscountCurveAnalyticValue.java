/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;

/**
 * An {@link AnalyticValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class DiscountCurveAnalyticValue extends AbstractAnalyticValue {
  public DiscountCurveAnalyticValue(AnalyticValueDefinition definition, DiscountCurve value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue scaleForPosition(BigDecimal quantity) {
    return new DiscountCurveAnalyticValue(getDefinition(), (DiscountCurve) getValue());
  }

}
