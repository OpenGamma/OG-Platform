/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * 
 */
public class PortfolioHistoricalVaRDefaultPropertiesFunction extends NormalHistoricalVaRDefaultPropertiesFunction {

  public PortfolioHistoricalVaRDefaultPropertiesFunction(final String samplingPeriod, final String scheduleCalculator, final String samplingCalculator, 
      final String meanCalculator, final String stdDevCalculator, final String confidenceLevel, final String horizon) {
    super(samplingPeriod, scheduleCalculator, samplingCalculator, meanCalculator, stdDevCalculator, confidenceLevel, horizon,
        ComputationTargetType.PORTFOLIO_NODE);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PORTFOLIO_NODE;
  }

}
