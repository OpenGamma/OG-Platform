/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;

/**
 * 
 */
public class PositionWeightFunction extends AbstractWeightFunction {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION);
  }

  @Override
  protected ComputationTargetSpecification getValueTarget(final ComputationTarget target) {
    return target.getLeafSpecification();
  }

  @Override
  protected ComputationTargetReference getParentTarget(final ComputationTarget target) {
    return target.getContextSpecification();
  }

}
