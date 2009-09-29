/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;
import java.util.Map;

import com.opengamma.financial.greeks.Greek;

/**
 * An {@link AnalyticValue} for a single double-precision floating point value.
 *
 * @author kirk
 */
public class GreeksResultAnalyticValue extends AbstractAnalyticValue<Map<Greek, Map<String, Double>>> {
  public GreeksResultAnalyticValue(GreeksResultValueDefinition definition, Map<Greek, Map<String, Double>> value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<Map<Greek, Map<String, Double>>> scaleForPosition(BigDecimal quantity) {
    return this;
  }

}
