/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;

import com.opengamma.financial.greeks.GreekResultCollection;

/**
 * An {@link AnalyticValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class GreeksResultAnalyticValue extends AbstractAnalyticValue<GreekResultCollection> {
  public GreeksResultAnalyticValue(GreeksResultValueDefinition definition, GreekResultCollection value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<GreekResultCollection> scaleForPosition(BigDecimal quantity) {
    return this;
  }

}
