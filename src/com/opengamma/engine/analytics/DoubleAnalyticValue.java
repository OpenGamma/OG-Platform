/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;

/**
 * An {@link AnalyticValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class DoubleAnalyticValue extends AnalyticValueImpl<Double> {
  public DoubleAnalyticValue(AnalyticValueDefinition<Double> definition, Double value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<Double> scaleForPosition(BigDecimal quantity) {
    double qtyAsDouble = quantity.doubleValue();
    double valueAsDouble = (Double)getValue();
    double scaledValue = qtyAsDouble * valueAsDouble;
    return new DoubleAnalyticValue(getDefinition(), scaledValue);
  }

}
