/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.Map;

import com.opengamma.core.Attributable;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;

/**
 * Dummy function to inject default properties from a trades or security's attributes into the dependency graph.
 */
public class AttributableDefaultPropertyFunction extends PositionOrAttributableDefaultPropertyFunction {

  public AttributableDefaultPropertyFunction() {
    super(ComputationTargetType.TRADE.or(ComputationTargetType.SECURITY));
  }

  @Override
  protected Map<String, String> getAttributes(final ComputationTarget target) {
    return ((Attributable) target.getValue()).getAttributes();
  }

}
