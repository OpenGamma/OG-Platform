/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * 
 */
public class PositionPortfolioAnalysisDefaultPropertiesFunction extends PortfolioAnalysisDefaultPropertiesFunction {

  public PositionPortfolioAnalysisDefaultPropertiesFunction(final String samplingPeriodName, final String scheduleCalculatorName, final String samplingFunctionName, 
      final String returnCalculatorName, final String stdDevCalculatorName, final String expectedExcessReturnCalculatorName, final String... valueNames) {
    super(samplingPeriodName, scheduleCalculatorName, samplingFunctionName, returnCalculatorName, stdDevCalculatorName, expectedExcessReturnCalculatorName, 
        valueNames, ComputationTargetType.POSITION);
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

}
