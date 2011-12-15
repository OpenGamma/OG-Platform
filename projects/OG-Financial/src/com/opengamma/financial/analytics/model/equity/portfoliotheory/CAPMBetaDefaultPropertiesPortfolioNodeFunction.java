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
public class CAPMBetaDefaultPropertiesPortfolioNodeFunction extends CAPMBetaDefaultPropertiesFunction {
  
  public CAPMBetaDefaultPropertiesPortfolioNodeFunction(final String samplingPeriodName, final String scheduleCalculatorName, final String samplingFunctionName, 
      final String returnCalculatorName, final String covarianceCalculatorName, final String varianceCalculatorName) {
    super(samplingPeriodName, scheduleCalculatorName, samplingFunctionName, returnCalculatorName, covarianceCalculatorName, varianceCalculatorName, ComputationTargetType.PORTFOLIO_NODE);
  }
}
