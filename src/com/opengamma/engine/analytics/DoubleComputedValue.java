/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;

/**
 * An {@link ComputedValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class DoubleComputedValue extends ComputedValueImpl<Double> {
  public DoubleComputedValue(AnalyticValueDefinition<Double> definition, Double value) {
    super(definition, value);
  }

  @Override
  public ComputedValue<Double> scaleForPosition(BigDecimal quantity) {
    double qtyAsDouble = quantity.doubleValue();
    double valueAsDouble = (Double)getValue();
    double scaledValue = qtyAsDouble * valueAsDouble;
    return new DoubleComputedValue(getDefinition(), scaledValue);
  }

}
