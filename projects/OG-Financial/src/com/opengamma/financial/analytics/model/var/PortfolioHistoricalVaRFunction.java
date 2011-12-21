/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * 
 *
 */
public class PortfolioHistoricalVaRFunction extends NormalHistoricalVaRFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
