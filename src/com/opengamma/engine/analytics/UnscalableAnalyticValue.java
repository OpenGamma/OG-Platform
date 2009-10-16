/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.math.BigDecimal;

/**
 * 
 *
 * @author jim
 */
public class UnscalableAnalyticValue<T> extends AnalyticValueImpl<T> {

  /**
   * @param definition
   * @param value
   */
  protected UnscalableAnalyticValue(AnalyticValueDefinition<T> definition, T value) {
    super(definition, value);
  }

  @Override
  public AnalyticValue<T> scaleForPosition(BigDecimal quantity) {
    throw new UnsupportedOperationException("Cannot scale an UnscalableAnalyticValue");
  }

}
