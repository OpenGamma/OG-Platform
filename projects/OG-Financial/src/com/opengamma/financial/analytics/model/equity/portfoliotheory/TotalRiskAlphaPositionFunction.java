/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class TotalRiskAlphaPositionFunction extends TotalRiskAlphaFunction {

  public TotalRiskAlphaPositionFunction(final String returnCalculatorName, final String expectedAssetReturnCalculatorName, final String expectedRiskFreeReturnCalculatorName,
      final String expectedMarketReturnCalculatorName, final String assetStandardDeviationCalculatorName, final String marketStandardDeviationCalculatorName, final String startDate) {
    super(returnCalculatorName, expectedAssetReturnCalculatorName, expectedRiskFreeReturnCalculatorName, expectedMarketReturnCalculatorName, assetStandardDeviationCalculatorName,
        marketStandardDeviationCalculatorName, startDate);
  }

  @Override
  public Object getTarget(final ComputationTarget target) {
    return target.getPosition();
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public String getShortName() {
    return "TotalRiskAlphaPositionFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
