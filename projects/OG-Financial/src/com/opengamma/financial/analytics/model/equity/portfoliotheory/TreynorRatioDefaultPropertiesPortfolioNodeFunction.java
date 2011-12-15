/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import com.opengamma.engine.ComputationTargetType;

/**
 * 
 */
public class TreynorRatioDefaultPropertiesPortfolioNodeFunction extends TreynorRatioDefaultPropertiesFunction {

  public TreynorRatioDefaultPropertiesPortfolioNodeFunction(final String samplingPeriodName, final String scheduleCalculatorName, final String samplingFunctionName, 
      final String returnCalculatorName, final String stdDevCalculatorName, final String excessReturnCalculatorName, final String covarianceCalculatorName,
      final String varianceCalculatorName) {
    super(samplingPeriodName, scheduleCalculatorName, samplingFunctionName, returnCalculatorName, stdDevCalculatorName, excessReturnCalculatorName, 
        covarianceCalculatorName, varianceCalculatorName, ComputationTargetType.PORTFOLIO_NODE);
  }

}
