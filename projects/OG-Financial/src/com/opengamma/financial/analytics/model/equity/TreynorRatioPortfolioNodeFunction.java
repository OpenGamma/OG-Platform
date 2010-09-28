/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * 
 */
public class TreynorRatioPortfolioNodeFunction extends TreynorRatioFunction {

  public TreynorRatioPortfolioNodeFunction(final String expectedReturnCalculatorName, final String standardDeviationCalculatorName, final String startDate) {
    super(expectedReturnCalculatorName, standardDeviationCalculatorName, startDate);
  }

  @Override
  public Object getTarget(final ComputationTarget target) {
    return target.getPortfolioNode();
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public String getShortName() {
    return "TreynorRatioPortfolioNodeFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
