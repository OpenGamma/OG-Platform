/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.Map;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;

/**
 * Dummy function to inject default properties from a position's attributes into the dependency graph.
 */
public class PositionDefaultPropertyFunction extends PositionOrTradeDefaultPropertyFunction {

  public PositionDefaultPropertyFunction() {
    super(ComputationTargetType.POSITION);
  }

  @Override
  protected Map<String, String> getAttributes(final ComputationTarget target) {
    return target.getPosition().getAttributes();
  }

}
