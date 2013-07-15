/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;

/**
 * 
 */
public class PortfolioNodeWeightFunction extends AbstractWeightFunction {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getPortfolioNode().getParentNodeId() != null;
  }

  @Override
  protected ComputationTargetSpecification getValueTarget(final ComputationTarget target) {
    return target.toSpecification();
  }

  @Override
  protected ComputationTargetSpecification getParentTarget(final ComputationTarget target) {
    return new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, target.getPortfolioNode().getParentNodeId());
  }

}
