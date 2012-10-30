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
 * Dummy function to inject default properties from a trades's attributes into the dependency graph.
 */
public class TradeDefaultPropertyFunction extends PositionOrTradeDefaultPropertyFunction {

  public TradeDefaultPropertyFunction() {
    super(ComputationTargetType.TRADE);
  }

  @Override
  protected Map<String, String> getAttributes(final ComputationTarget target) {
    return target.getTrade().getAttributes();
  }

}
