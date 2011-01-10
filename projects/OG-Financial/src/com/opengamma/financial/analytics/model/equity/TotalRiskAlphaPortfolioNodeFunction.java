/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class TotalRiskAlphaPortfolioNodeFunction extends TotalRiskAlphaFunction {

  public TotalRiskAlphaPortfolioNodeFunction(final String returnCalculatorName, final String expectedAssetReturnCalculatorName, final String expectedRiskFreeReturnCalculatorName,
      final String expectedMarketReturnCalculatorName, final String assetStandardDeviationCalculatorName, final String marketStandardDeviationCalculatorName, final String startDate) {
    super(returnCalculatorName, expectedAssetReturnCalculatorName, expectedRiskFreeReturnCalculatorName, expectedMarketReturnCalculatorName, assetStandardDeviationCalculatorName,
        marketStandardDeviationCalculatorName, startDate);
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
    return "TotalRiskAlphaPortfolioNodeFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
