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
public class CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction extends CAPMFromRegressionDefaultPropertiesFunction {

  public CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction(final String samplingPeriodName, final String scheduleCalculatorName, final String samplingFunctionName, 
      final String returnCalculatorName) {
    super(samplingPeriodName, scheduleCalculatorName, samplingFunctionName, returnCalculatorName, ComputationTargetType.PORTFOLIO_NODE);
  }

}
